package com.superyc.heiniu.api.device;

import com.superyc.heiniu.api.device.impl.DeviceApiMqttImpl;
import com.superyc.heiniu.api.device.impl.DeviceApiPandaImpl;
import org.springframework.stereotype.Component;

/**
 * 工厂模式 控制DeviceApi的实现
 */
@Component
public class DeviceApiFactory {

    /**
     * 根据设备号选择不同的通信协议
     */
    public static DeviceApi getApi(String deviceNum) {
        // TODO 修改设备号匹配规则
        if (deviceNum.startsWith("4")) {
            return new DeviceApiPandaImpl();
        } else {
            return new DeviceApiMqttImpl();
        }
    }
}
