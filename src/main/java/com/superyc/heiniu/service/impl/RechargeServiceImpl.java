package com.superyc.heiniu.service.impl;

import com.superyc.heiniu.api.wx.WxApi;
import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.bean.RechargeOrder;
import com.superyc.heiniu.bean.RechargeRank;
import com.superyc.heiniu.enums.RechargeOrderStatusEnum;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.mapper.RechargeOrderMapper;
import com.superyc.heiniu.mapper.RechargeRankMapper;
import com.superyc.heiniu.mapper.UserMapper;
import com.superyc.heiniu.service.RechargeService;
import com.superyc.heiniu.utils.OrderNumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
    private RechargeRankMapper rechargeRankMapper;
    private UserMapper userMapper;
    private RechargeOrderMapper rechargeOrderMapper;

    @Override
    public CommonResponse getRechargeRank() {
        List<RechargeRank> rechargeRanks = rechargeRankMapper.selectAll();
        return CommonResponse.success(rechargeRanks);
    }

    @Override
    @Transactional
    public CommonResponse recharge(int userId, int rankId, String userIp) throws InvocationTargetException,
            NoSuchMethodException, JAXBException, IllegalAccessException, IOException {
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
        order.setStatus(RechargeOrderStatusEnum.WAITING_PAY.getValue());
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
    public WxApi.AfterPayCallbackResponse callback(WxApi.AfterPayCallbackParam callbackResult) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ParseException {
        boolean b = WxApi.checkSign(callbackResult, WxApi.AfterPayCallbackParam.class);
        if (!b) {
            WxApi.AfterPayCallbackResponse response = new WxApi.AfterPayCallbackResponse();
            response.setReturnCode("FAIL");
            response.setReturnMsg("签名校验失败");
            return response;
        }

        RechargeOrder order = rechargeOrderMapper.selectByOrderNumber(callbackResult.getOutTradeNo());
        order.setTransactionId(callbackResult.getTransactionId());
        order.setStatus(RechargeOrderStatusEnum.FINISH_PAY.getValue());
        order.setFinishTime(new Timestamp(new SimpleDateFormat("yyyyMMddHHmmss").parse(callbackResult.getTimeEnd()).getTime()));
        order.setPaymentType(callbackResult.getBankType());

        rechargeOrderMapper.updateByPrimaryKey(order);
        userMapper.updateBalance(order.getUserId(), order.getTotalMoney());

        // TODO 给代理商转账

        WxApi.AfterPayCallbackResponse response = new WxApi.AfterPayCallbackResponse();
        response.setReturnCode("SUCCESS");
        response.setReturnMsg("成功");
        return response;
    }

    @Override
    public CommonResponse getRechargeList(int userId, int page, int size) {
        List<RechargeOrder> orderList = rechargeOrderMapper.getOrderList(userId, page, size);
        orderList.forEach((order) -> {
            order.setTransactionId(null);
            order.setUserId(null);
            order.setStatus(null);
        });
        return CommonResponse.success(orderList);
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
}
