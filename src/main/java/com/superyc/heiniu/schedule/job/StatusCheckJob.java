package com.superyc.heiniu.schedule.job;

import com.superyc.heiniu.api.device.DeviceApi;
import com.superyc.heiniu.api.device.DeviceApiFactory;
import com.superyc.heiniu.service.DeviceService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 设备状态检查
 */
@Component
public class StatusCheckJob implements Job {
    private final Logger LOG = LoggerFactory.getLogger(StatusCheckJob.class);

    private int userId;
    private int deviceId;
    private String deviceNum;
    private int pathId;

    private DeviceService deviceService;

    @Override
    public void execute(JobExecutionContext jobContext) {
        Map<String, String> deviceState = null;
        DeviceApi deviceApi = DeviceApiFactory.getApi(deviceNum);
        try {
            deviceState = deviceApi.getDeviceState(deviceNum);
        } catch (InterruptedException | IOException | MqttException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        if (deviceState == null) {
            return;
        }

        String s = deviceState.get(String.valueOf(pathId));
        if (Integer.parseInt(s) > 0) {
            LOG.info("端口输出正常  deviceId:{}, pathId:{}", deviceId, pathId);
        } else {
            LOG.info("端口关闭  deviceId:{}, pathId:{}", deviceId, pathId);
            try {
                deviceService.stopCharge(deviceId, pathId, userId);
            } catch (NoSuchMethodException | IOException | IllegalAccessException | InvocationTargetException e) {
                LOG.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceNum(String deviceNum) {
        this.deviceNum = deviceNum;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    @Autowired
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
