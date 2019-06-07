package com.superyc.heiniu.api.device;

import com.superyc.heiniu.bean.CommonResponse;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 充电设备控制
 */
public interface DeviceApi {
    /**
     * 根据设备号返回设备信息
     * @param deviceNum 设备号
     * @return 端口对应的功率。map的key为端口号，value为端口的功率
     */
    Map<String, String> getDeviceState(String deviceNum) throws InterruptedException, IOException, MqttException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException;

    CommonResponse startPath(String deviceNum, int pathId) throws MqttException, InterruptedException, IOException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException;


    CommonResponse stop(String deviceNum, int pathId) throws InvocationTargetException, NoSuchMethodException,
            IOException, IllegalAccessException;
}
