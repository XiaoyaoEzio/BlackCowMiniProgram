package com.superyc.heiniu.service.impl;

import com.superyc.heiniu.api.device.DeviceApiFactory;
import com.superyc.heiniu.bean.*;
import com.superyc.heiniu.enums.ChargePayMode;
import com.superyc.heiniu.enums.OrderStatusEnum;
import com.superyc.heiniu.enums.ProxyModeEnum;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.exception.PandaCloudException;
import com.superyc.heiniu.mapper.*;
import com.superyc.heiniu.schedule.QuartzJobUtils;
import com.superyc.heiniu.service.DeviceService;
import com.superyc.heiniu.utils.OrderNumberUtils;
import com.superyc.heiniu.utils.TimeUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class DeviceServiceImpl implements DeviceService {
    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private UserMapper userMapper;
    private DeviceMapper deviceMapper;
    private ChargeRankMapper chargeRankMapper;
    private ElectricityPriceMapper electricityPriceMapper;
    private ChargeOrderMapper chargeOrderMapper;
    private ProxyClientMapper proxyClientMapper;

    @Override
    public CommonResponse getDeviceInfo(int deviceId) throws IOException, InterruptedException, MqttException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // 查询设备信息，封装
        Device device = deviceMapper.selectByPrimaryKey(deviceId);
        if (device == null) {
            return CommonResponse.failure(ResponseCodeEnum.DEVICE_NOT_FOUND);
        }

        StateResult result = new StateResult();
        result.setId(deviceId);
        result.setDeviceName(device.getDeviceName());
        String deviceNum = device.getDeviceNumber();
        result.setDeviceNumber(deviceNum);
        result.setPricePerHour(electricityPriceMapper.selectPriceByDeviceId(device.getId()));
        result.setChargeRank(chargeRankMapper.selectAll());

        Map<String, String> stateMap = DeviceApiFactory.getApi(deviceNum).getDeviceState(deviceNum);
        List<PortInfo> ports = new ArrayList<>();
        for (Map.Entry<String, String> entry : stateMap.entrySet()) {
            // key为-1, 说明熊猫云服务器返回值异常
            if ("-1".equals(entry.getKey())) {
                LOG.error("panda cloud error: {}", entry.getValue());
                return CommonResponse.failure(ResponseCodeEnum.PANDA_CLOUD_ERROR,
                        "panda cloud error：" + entry.getValue());
            }

            PortInfo port = new PortInfo();
            port.setId(Integer.parseInt(entry.getKey()));
            // 功率大于0说明在使用，返回1，没在使用，返回0
            port.setStatus(Integer.parseInt(entry.getValue()) > 0 ? 1 : 0);
            ports.add(port);
        }

        result.setPorts(ports);
        return CommonResponse.success(result);
    }

    /**
     * 开启充电端口
     *
     * 业务流程：
     * 1. 查询对应用户和设备，判断是否存在
     * 2. 查询是否存在未完成充电订单
     * 3. 计算充电金额
     * 4. 判断支付模式为用户个人支付或者集团支付
     * 5. 生成订单信息
     * 6. 开启设备端口
     * 7. 启动quartz定时关闭
     *
     * @param deviceId 设备id，不同于设备编号
     * @param pathId   端口id
     * @param rankId   充电档位
     * @param userId   用户id
     */
    @Override
    @Transactional
    public CommonResponse startCharge(int deviceId, int pathId, int rankId, int userId) throws InterruptedException,
            IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, MqttException {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return CommonResponse.failure(ResponseCodeEnum.USER_NOT_FOUND);
        }

        Device device = deviceMapper.selectByPrimaryKey(deviceId);
        if (device == null) {
            return CommonResponse.failure(ResponseCodeEnum.DEVICE_NOT_FOUND);
        }

        ChargeOrder unfinishedOrder = chargeOrderMapper.selectFirstUnfinishedOrderByUserId(userId);
        if (unfinishedOrder != null) {
            return new CommonResponse(ResponseCodeEnum.HAS_UNFINISHED_ORDER, unfinishedOrder.getOrderNumber());
        }

        int userGroupId = user.getGroupId();
        int deviceSecondProxyId = device.getSecondProxyId();

        if (userGroupId == 0) {
            // 如果用户为绑定代理商，则绑定至当前代理商
            user.setGroupId(deviceSecondProxyId);
            userMapper.updateByPrimaryKey(user);
        } else if (userGroupId != deviceSecondProxyId) {
            // 用户所属代理商与设备代理商不同
            return CommonResponse.failure(ResponseCodeEnum.NOT_MATCHING);
        }

        // 计算消费金额
        Integer chargeTime = chargeRankMapper.selectByPrimaryKey(rankId).getTime();
        int pricePerHour = electricityPriceMapper.selectPriceByDeviceId(deviceId);
        int totalFee = pricePerHour * chargeTime;

        // 如果设备为集团代理模式，费用由集团支付
        int payMode;
        if (ProxyModeEnum.GROUP_PROXY.getValue() == device.getProxyMode()) {
            payMode = ChargePayMode.GROUP.getValue();
        } else {
            // 个人模式
            Integer balance = user.getBalance();
            payMode = ChargePayMode.USER.getValue();
            if (balance < totalFee) {
                return CommonResponse.failure(ResponseCodeEnum.INSUFFICIENT_USER_BALANCE);
            }
        }

        // 生成订单
        ChargeOrder chargeOrder = new ChargeOrder();
        chargeOrder.setCreationTime(new Timestamp(System.currentTimeMillis()));
        chargeOrder.setDeviceId(deviceId);
        chargeOrder.setFinishTime(chargeOrder.getCreationTime());
        chargeOrder.setOrderNumber(OrderNumberUtils.getConsumeOrderNumber());
        chargeOrder.setPathId(pathId);
        chargeOrder.setUserId(userId);
        chargeOrder.setStatus(OrderStatusEnum.DEALING.getValue());
        chargeOrder.setMoney(totalFee);
        chargeOrder.setPayMode(payMode);
        chargeOrder.setRankId(rankId);
        chargeOrderMapper.insert(chargeOrder);

        // 开启端口
        // TODO 改为抛异常

        String deviceNum = device.getDeviceNumber();
        CommonResponse startPathResult = DeviceApiFactory.getApi(deviceNum).startPath(deviceNum, pathId);
        if (!CommonResponse.isSuccess(startPathResult)) {
            return startPathResult;
        }

        // TODO 改为抛异常
        boolean startResult = QuartzJobUtils.startCharge(chargeTime, userId, deviceId, deviceNum, pathId);
        if (!startResult) {
            return CommonResponse.failure(ResponseCodeEnum.QUARTZ_START_ERROR);
        }

        return CommonResponse.success(chargeOrder);
    }

    /**
     * 停止充电，结束点单，扣费
     */
    @Override
    @Transactional
    public CommonResponse stopCharge(int deviceId, int pathId, int userId) throws NoSuchMethodException, IOException,
            IllegalAccessException, InvocationTargetException {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return CommonResponse.failure(ResponseCodeEnum.USER_NOT_FOUND);
        }

        Device device = deviceMapper.selectByPrimaryKey(deviceId);
        if (device == null) {
            return CommonResponse.failure(ResponseCodeEnum.DEVICE_NOT_FOUND);
        }

        ChargeOrder unfinishedOrder = chargeOrderMapper.selectFirstUnfinishedOrderByUserId(userId);
        if (unfinishedOrder == null) {
            return CommonResponse.failure(ResponseCodeEnum.HAS_NO_CHARGING_ORDER);
        }

        unfinishedOrder.setFinishTime(new Timestamp(System.currentTimeMillis()));
        unfinishedOrder.setStatus(OrderStatusEnum.FINISHED.getValue());

        //**** 计算充电时长 ****//
        int actualTime = TimeUtils.getHours(unfinishedOrder.getCreationTime(), unfinishedOrder.getFinishTime());
        int selectTime = chargeRankMapper.selectByPrimaryKey(unfinishedOrder.getRankId()).getTime();
        int interval = 1000 * 60 * 30;
        double chargeTime;

        /*
         * 充电时间略大于选择时间（考虑到系统延时） 或者 实际充电时间与选择时间间隔小于半小时。按照选择时间算
         * 实际充电时间与选择时间相差超过半小时，按照 实际充电时间+半小时 算
         */
        if (actualTime >= selectTime || selectTime - actualTime <= interval) {
            chargeTime = selectTime;
        } else {
            chargeTime = actualTime + 0.5;
        }

        // **** 扣费 **** //
        int payment = new Double(chargeTime * electricityPriceMapper.selectPriceByDeviceId(deviceId)).intValue();
        Integer payMode = unfinishedOrder.getPayMode();
        if (payMode.equals(ChargePayMode.USER.getValue())) {
            userMapper.updateBalance(userId, -payment);
        } else {
            proxyClientMapper.updateBalance(user.getGroupId(), -payment);
        }
        unfinishedOrder.setMoney(payment);
        chargeOrderMapper.updateByPrimaryKey(unfinishedOrder);

        // **** 关闭端口 **** //
        String deviceNum = device.getDeviceNumber();
        CommonResponse stopResult = DeviceApiFactory.getApi(deviceNum).stop(deviceNum, pathId);
        if (stopResult.getStatus() != ResponseCodeEnum.OK.getValue()) {
            LOG.error("close path error\n\tdeviceId:{}, pathId:{}", deviceId, pathId);
            throw new PandaCloudException(stopResult.getMessage());
        }

        // **** 停止定时任务 **** //
        QuartzJobUtils.stopJobs(userId);
        return CommonResponse.success();
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Autowired
    public void setDeviceMapper(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    @Autowired
    public void setChargeRankMapper(ChargeRankMapper chargeRankMapper) {
        this.chargeRankMapper = chargeRankMapper;
    }

    @Autowired
    public void setElectricityPriceMapper(ElectricityPriceMapper electricityPriceMapper) {
        this.electricityPriceMapper = electricityPriceMapper;
    }

    @Autowired
    public void setChargeOrderMapper(ChargeOrderMapper chargeOrderMapper) {
        this.chargeOrderMapper = chargeOrderMapper;
    }

    @Autowired
    public void setProxyClientMapper(ProxyClientMapper proxyClientMapper) {
        this.proxyClientMapper = proxyClientMapper;
    }

    /**
     * 用于封装返回给前端的参数
     */
    @SuppressWarnings("unused")
    public class StateResult {
        private int id;
        private String deviceName;
        private String deviceNumber;
        private int pricePerHour;
        private List<PortInfo> ports;
        private List<ChargeRank> chargeRank;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDeviceName() {
            return deviceName;
        }

        void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceNumber() {
            return deviceNumber;
        }

        void setDeviceNumber(String deviceNumber) {
            this.deviceNumber = deviceNumber;
        }

        public int getPricePerHour() {
            return pricePerHour;
        }

        void setPricePerHour(int pricePerHour) {
            this.pricePerHour = pricePerHour;
        }

        public List<PortInfo> getPorts() {
            return ports;
        }

        void setPorts(List<PortInfo> ports) {
            this.ports = ports;
        }

        public List<ChargeRank> getChargeRank() {
            return chargeRank;
        }

        void setChargeRank(List<ChargeRank> chargeRank) {
            this.chargeRank = chargeRank;
        }
    }

    /**
     * 端口信息
     */
    @SuppressWarnings("unused")
    public static class PortInfo {
        private int id;
        private int status;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
