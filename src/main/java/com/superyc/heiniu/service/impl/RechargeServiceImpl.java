package com.superyc.heiniu.service.impl;

import com.superyc.heiniu.api.wx.WxApi;
import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.bean.RechargeOrder;
import com.superyc.heiniu.bean.RechargeRank;
import com.superyc.heiniu.bean.TransferOrder;
import com.superyc.heiniu.enums.OrderStatusEnum;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.exception.TransferException;
import com.superyc.heiniu.mapper.*;
import com.superyc.heiniu.service.RechargeService;
import com.superyc.heiniu.utils.OrderNumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 充值
 */
@Service
public class RechargeServiceImpl implements RechargeService {
    private final Logger LOG = LoggerFactory.getLogger(RechargeServiceImpl.class);

    private RechargeRankMapper rechargeRankMapper;
    private UserMapper userMapper;
    private RechargeOrderMapper rechargeOrderMapper;
    private TransferOrderMapper transferOrderMapper;
    private ProxyCilentBankInfoMapper bankInfoMapper;

    @Override
    public CommonResponse getRechargeRank() {
        List<RechargeRank> rechargeRanks = rechargeRankMapper.selectAll();
        return CommonResponse.success(rechargeRanks);
    }

    @Override
    @Transactional
    public CommonResponse recharge(int userId, int rankId, String userIp) throws IOException {
        // 检查是否有未完成订单
        RechargeOrder uOrder = rechargeOrderMapper.getUnfinishedOrder(userId);
        if (uOrder != null) {
            return CommonResponse.failure(ResponseCodeEnum.HAS_UNFINISHED_ORDER);
        }

        RechargeRank rechargeRank = rechargeRankMapper.selectByPrimaryKey(rankId);
        Integer payment = rechargeRank.getPayment();
        Integer total = payment + rechargeRank.getGift();

        // 生成充值订单
        RechargeOrder order = new RechargeOrder();
        order.setCreationTime(new Timestamp(new Date().getTime()));
        order.setFinishTime(order.getCreationTime());
        order.setOrderNumber(OrderNumberUtils.getRechargeOrderNumber());
        order.setPaymentMoney(payment);
        order.setTotalMoney(total);
        order.setPaymentType("WAITING_PAY");
        order.setStatus(OrderStatusEnum.DEALING.getValue());
        order.setUserId(userId);
        order.setTransactionId("-1");
        rechargeOrderMapper.insert(order);

        // 调用统一支付API
        CommonResponse tempResponse = WxApi.unifiedOrder(order.getOrderNumber(),
                userMapper.getMiniOpenIdByUserId(userId), payment.toString(), userIp);

        if (tempResponse.getStatus() != ResponseCodeEnum.OK.getValue()) {
            return tempResponse;
        }

        // 统一下单成功，生成响应
        String prePayId = (String) tempResponse.getData();
        return WxApi.reSign(prePayId);
    }

    @Override
    @Transactional
    public WxApi.AfterPayCallbackResponse callback(WxApi.AfterPayCallbackParam callbackResult) throws IOException {
        WxApi.AfterPayCallbackResponse response = new WxApi.AfterPayCallbackResponse();
        boolean b = WxApi.checkSign(callbackResult, WxApi.AfterPayCallbackParam.class);
        if (!b) {
            response.setReturnCode("FAIL");
            response.setReturnMsg("签名校验失败");
            return response;
        }

        RechargeOrder order = rechargeOrderMapper.selectByOrderNumber(callbackResult.getOutTradeNo());
        order.setTransactionId(callbackResult.getTransactionId());
        order.setStatus(OrderStatusEnum.FINISHED.getValue());
        try {
            order.setFinishTime(new Timestamp(new SimpleDateFormat("yyyyMMddHHmmss").parse(callbackResult.getTimeEnd()).getTime()));
        } catch (ParseException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        order.setPaymentType(callbackResult.getBankType());

        rechargeOrderMapper.updateByPrimaryKey(order);
        userMapper.updateBalance(order.getUserId(), order.getTotalMoney());

        // 给代理商转账
        int groupId = userMapper.getGroupId(order.getUserId());
        TransferOrder transferOrder = new TransferOrder();
        transferOrder.setAmount(order.getPaymentMoney());
        transferOrder.setCommissionAmount(0);
        transferOrder.setCreationTime(new Date(System.currentTimeMillis()));
        transferOrder.setFinishedTime(transferOrder.getCreationTime());
        transferOrder.setOrderNumber(OrderNumberUtils.getTransferOrderNumber());
        transferOrder.setProxyId(groupId);
        transferOrder.setStatus(OrderStatusEnum.DEALING.getValue());
        transferOrder.setWxPaymentNumber("");
        transferOrderMapper.insert(transferOrder);
        transfer(transferOrder);

        response.setReturnCode("SUCCESS");
        response.setReturnMsg("成功");
        return response;
    }

    @Override
    public CommonResponse getRechargeList(int userId, int page, int size) {
        int offset = page * size;
        List<RechargeOrder> orderList = rechargeOrderMapper.getOrderList(userId, offset, size);
        orderList.forEach((order) -> {
            order.setTransactionId(null);
            order.setUserId(null);
            order.setStatus(null);
        });
        return CommonResponse.success(orderList);
    }

    @Async
    @Transactional
    public void transfer(TransferOrder transferOrder) throws IOException {
        WxApi.TransferResult transferResult =
                WxApi.transferToBank(bankInfoMapper.selectByPrimaryKey(transferOrder.getProxyId()), transferOrder);

        if (WxApi.SUCCESS.equals(transferResult.getResultCode()) && WxApi.SUCCESS.equals(transferResult.getReturnCode())) {
            transferOrder.setCommissionAmount(transferResult.getCmmsAmt());
            transferOrder.setWxPaymentNumber(transferResult.getPaymentNo());
            transferOrder.setFinishedTime(new Date(System.currentTimeMillis()));
            transferOrder.setStatus(OrderStatusEnum.FINISHED.getValue());
            transferOrderMapper.updateByPrimaryKey(transferOrder);
        } else {
            LOG.error("transfer response:{}", transferResult.toString());
            throw new TransferException("wx response error");
        }
    }

    @Autowired
    public void setRechargeRankMapper(RechargeRankMapper rechargeRankMapper) {
        this.rechargeRankMapper = rechargeRankMapper;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Autowired
    public void setRechargeOrderMapper(RechargeOrderMapper rechargeOrderMapper) {
        this.rechargeOrderMapper = rechargeOrderMapper;
    }

    @Autowired
    public void setTransferOrderMapper(TransferOrderMapper transferOrderMapper) {
        this.transferOrderMapper = transferOrderMapper;
    }

    @Autowired
    public void setBankInfoMapper(ProxyCilentBankInfoMapper bankInfoMapper) {
        this.bankInfoMapper = bankInfoMapper;
    }
}
