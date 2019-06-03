package com.superyc.heiniu.service.impl;

import com.superyc.heiniu.api.device.DeviceApiFactory;
import com.superyc.heiniu.bean.*;
import com.superyc.heiniu.enums.ChargeOrderStatusEnum;
import com.superyc.heiniu.enums.ChargePayMode;
import com.superyc.heiniu.enums.ProxyModeEnum;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.mapper.*;
import com.superyc.heiniu.schedule.QuartzJobUtils;
import com.superyc.heiniu.service.DeviceService;
import com.superyc.heiniu.utils.OrderNumberUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
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
    private UserMapper userMapper;
    private DeviceMapper deviceMapper;
    private ChargeRankMapper chargeRankMapper;
    private ElectricityPriceMapper electricityPriceMapper;
    private ChargeOrderMapper chargeOrderMapper;
    private ProxyClientMapper proxyClientMapper;

    @Override
    public CommonResponse getDeviceInfo(String deviceNum) throws IOException, InterruptedException, MqttException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // 查询设备信息，封装
        Device device = deviceMapper.selectByDeviceNum(deviceNum);
        if (device == null) {
            return CommonResponse.failure(ResponseCodeEnum.DEVICE_NOT_FOUND);
        }

        StateResult result = new StateResult();
        result.setId(device.getId());
        result.setDeviceName(device.getDeviceName());
        result.setDeviceNumber(deviceNum);
        result.setPricePerHour(electricityPriceMapper.selectPriceByDeviceId(device.getId()));
        result.setChargeRank(chargeRankMapper.selectAll());

        Map<String, String> stateMap = DeviceApiFactory.getApi(deviceNum).getDeviceState(deviceNum);
        List<PortInfo> ports = new ArrayList<>();
        for (Map.Entry<String, String> entry : stateMap.entrySet()) {
            // key为-1, 说明熊猫云服务器返回值异常
            if ("-1".equals(entry.getKey())) {
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
            IOException, MqttException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
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
        chargeOrder.setStatus(ChargeOrderStatusEnum.CHARGING.getValue());
        chargeOrder.setMoney(totalFee);
        chargeOrder.setPayMode(payMode);
        chargeOrderMapper.insert(chargeOrder);

        // 开启端口
        // TODO 改为抛异常
        CommonResponse startPathResult =
                DeviceApiFactory.getApi(device.getDeviceNumber()).startPath(device.getDeviceNumber(), pathId);
        if (!CommonResponse.isSuccess(startPathResult)) {
            return startPathResult;
        }

        // TODO 改为抛异常
        boolean startResult = QuartzJobUtils.startCharge(chargeTime, userId, device.getDeviceNumber(),
                String.valueOf(pathId));
        if (!startResult) {
            return CommonResponse.failure(ResponseCodeEnum.QUARTZ_START_ERROR);
        }

        return CommonResponse.success(chargeOrder);
    }

    @Override
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

        String deviceNum = device.getDeviceNumber();
        CommonResponse stopResult = DeviceApiFactory.getApi(deviceNum).stop(deviceNum, pathId);

        return null;
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

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceNumber() {
            return deviceNumber;
        }

        public void setDeviceNumber(String deviceNumber) {
            this.deviceNumber = deviceNumber;
        }

        public int getPricePerHour() {
            return pricePerHour;
        }

        public void setPricePerHour(int pricePerHour) {
            this.pricePerHour = pricePerHour;
        }

        public List<PortInfo> getPorts() {
            return ports;
        }

        public void setPorts(List<PortInfo> ports) {
            this.ports = ports;
        }

        public List<ChargeRank> getChargeRank() {
            return chargeRank;
        }

        public void setChargeRank(List<ChargeRank> chargeRank) {
            this.chargeRank = chargeRank;
        }
    }

    /**
     * 端口信息
     */
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
