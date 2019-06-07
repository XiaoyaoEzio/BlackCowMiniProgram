package com.superyc.heiniu.controller;

import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.service.DeviceService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * 硬件设备
 */
@Controller
@RequestMapping("/device")
@ResponseBody
public class DeviceController {
    private DeviceService deviceService;
    private String userIdKey;

    /**
     * 用户扫码入口
     */
    @RequestMapping("/info")
    public CommonResponse deviceInfo(
            @RequestParam("deviceId") int deviceId
    ) throws IOException, InterruptedException, MqttException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        return deviceService.getDeviceInfo(deviceId);
    }

    /**
     * 开始充电
     */
    @RequestMapping(value = "/handle", method = RequestMethod.POST)
    public CommonResponse handleCharge(
            @RequestParam("deviceId") int deviceId,
            @RequestParam("pathId") int pathId,
            @RequestParam("rankId") int rankId,
            HttpServletRequest request
    ) throws InterruptedException, MqttException, IOException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        // TODO 测试
        int userId = 1;
        //int userId = (int) request.getAttribute(userIdKey);
        return deviceService.startCharge(deviceId, pathId, rankId, userId);
    }

    /**
     * 停止充电
     */
    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    public CommonResponse stopCharge(
            @RequestParam("deviceId") int deviceId,
            @RequestParam("pathId") int pathId,
            HttpServletRequest request
    ) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        // TODO 测试
        int userId = 1;
        //int userId = (int) request.getAttribute(userIdKey);
        return deviceService.stopCharge(deviceId, pathId, userId);
    }

    @Value("#{session['userIdKey']}")
    public void setUserIdKey(String userIdKey) {
        this.userIdKey = userIdKey;
    }

    @Autowired
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
