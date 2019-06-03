package com.superyc.heiniu.service;

import com.superyc.heiniu.bean.CommonResponse;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public interface DeviceService {
    /**
     * 根据设备编号获取设备端口信息
     */
    CommonResponse getDeviceInfo(String deviceNum) throws IOException, InterruptedException, MqttException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException;

    /**
     *  开始充电
     */
    CommonResponse startCharge(int deviceId, int pathId, int rankId, int userId) throws InterruptedException,
            IOException, MqttException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;

    /**
     * 结束充电
     */
    CommonResponse stopCharge(int deviceId, int pathId, int userId) throws NoSuchMethodException, IOException,
            IllegalAccessException, InvocationTargetException;
}
