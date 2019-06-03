package com.superyc.heiniu.api.device.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.superyc.heiniu.api.device.DeviceApi;
import com.superyc.heiniu.api.wx.WxApi;
import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.utils.HttpUtils;
import com.superyc.heiniu.utils.JsonUtils;
import com.superyc.heiniu.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 使用熊猫云提供的开发文档实现对设备的控制
 */
@Component
public class DeviceApiPandaImpl implements DeviceApi {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static String appId;
    private static String secret;
    private static String mchId;
    private static String pandaUrl;

    private final String START = "start";
    private final String STOP = "reset";

    @Override
    public Map<String, String> getDeviceState(String deviceNum) throws IOException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Map<String, String> map = new HashMap<>();
        // 查询设备状态时，端口填00
        String STATE_COMMAND = "state";
        String pathId = "00";
        JsonNode response = operate(STATE_COMMAND, deviceNum, pathId);
        int result = response.get("result").asInt();
        JsonNode val = response.get("val");
        if (result != 1) {
            map.put("-1", val.get("message").asText());
            return map;
        }

        // result等于1，返回值正常，继续解析
        int online = val.get("online").asInt();
        if (online == -1) {
            map.put("-1", "device offline");
            return map;
        }

        JsonNode pathState = val.get("path_state");
        if (!pathState.isArray()) {
            map.put("-1", "path_state 不是数组");
            return map;
        }

        // 遍历端口信息
        Iterator<JsonNode> elements = pathState.elements();
        while (elements.hasNext()) {
            JsonNode node = elements.next();
            map.put(node.get("path").asText(), node.get("power").asText());
        }

        return map;
    }

    @Override
    public CommonResponse startPath(String deviceNum, int pathId) throws IOException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        String START_COMMAND = "start";
        return startOrStop(START_COMMAND, deviceNum, pathId);
    }

    @Override
    public CommonResponse stop(String deviceNum, int pathId) throws InvocationTargetException, NoSuchMethodException,
            IOException, IllegalAccessException {
        String STOP_COMMAND = "stop";
        return startOrStop(STOP_COMMAND, deviceNum, pathId);
    }

    /**
     * 开启或者关闭设备端口
     * @param command
     * @param deviceNum
     * @param pathId
     * @return
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws IllegalAccessException
     */
    private CommonResponse startOrStop(String command, String deviceNum, int pathId) throws InvocationTargetException
            , NoSuchMethodException, IOException, IllegalAccessException {
        String pathIdStr = pathId == 10 ? String.valueOf(pathId) : "0" + pathId;
        JsonNode response = operate(command, deviceNum, pathIdStr);
        int result = response.get("result").asInt();

        if (result == 1) {
            return CommonResponse.success();
        } else {
            JsonNode val = response.get("val");
            String message = val.get("message").asText();
            return CommonResponse.failure(ResponseCodeEnum.PANDA_CLOUD_ERROR, message);
        }
    }

    /**
     * 执行与熊猫云通信的工作
     *
     * @param command   命令（start|state|stop）
     * @param deviceNum 设备编号
     * @param pathId    端口编号
     */
    private JsonNode operate(String command, String deviceNum, String pathId) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, IOException {
        QueryParam param = new QueryParam();
        param.setInfo(command, deviceNum, pathId);
        String paramJson = JsonUtils.getString(param);

        log.debug("panda param: {}", paramJson);
        String response = HttpUtils.postJson(pandaUrl, paramJson);
        log.info("panda response: {}", response);

        return JsonUtils.readTree(response);
    }


    @Value("#{devicePanda['panda.url']}")
    public void setPandaUrl(String pandaUrl) {
        DeviceApiPandaImpl.pandaUrl = pandaUrl;
    }

    @Value("#{devicePanda['panda.appid']}")
    public void setAppId(String appId) {
        DeviceApiPandaImpl.appId = appId;
    }

    @Value("#{devicePanda['panda.appsecret']}")
    public void setSecret(String secret) {
        DeviceApiPandaImpl.secret = secret;
    }

    @Value("#{devicePanda['panda.mchid']}")
    public void setMchId(String mchId) {
        DeviceApiPandaImpl.mchId = mchId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class QueryParam {
        private String appid;
        private String mchid;
        private String nonce_str;
        private String sign;
        private String sign_type;
        private String time;
        private String device_id;
        private String device_path;
        private String openid;
        private String attach;
        private String command;

        public void setInfo(String command, String deviceNum, String pathId) throws NoSuchMethodException,
                IllegalAccessException, InvocationTargetException {
            this.setAppid(DeviceApiPandaImpl.appId);
            this.setMchid(DeviceApiPandaImpl.mchId);
            this.setNonce_str(RandomUtils.getUUID());
            this.setSign_type("MD5");
            this.setTime(Long.toString(RandomUtils.getCurrentTime() / 1000));
            this.setOpenid("BlackCow");
            this.setDevice_id(deviceNum);
            this.setDevice_path(pathId);
            this.setCommand(command);
            this.setSign(WxApi.generateSign(this, QueryParam.class, DeviceApiPandaImpl.secret));
        }

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getMchid() {
            return mchid;
        }

        public void setMchid(String mchid) {
            this.mchid = mchid;
        }

        public String getNonce_str() {
            return nonce_str;
        }

        public void setNonce_str(String nonce_str) {
            this.nonce_str = nonce_str;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getSign_type() {
            return sign_type;
        }

        public void setSign_type(String sign_type) {
            this.sign_type = sign_type;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDevice_id() {
            return device_id;
        }

        public void setDevice_id(String device_id) {
            this.device_id = device_id;
        }

        public String getDevice_path() {
            return device_path;
        }

        public void setDevice_path(String device_path) {
            this.device_path = device_path;
        }

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public String getAttach() {
            return attach;
        }

        public void setAttach(String attach) {
            this.attach = attach;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }
    }
}
