package com.superyc.heiniu.api.device.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyc.heiniu.api.device.DeviceApi;
import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.mqtt.BcMqttManager;
import com.superyc.heiniu.utils.JsonUtils;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class DeviceApiMqttImpl implements DeviceApi {
    private static final String STATE_QUERY = "^^state%%";
    private static final String START = "01";
    private static final String STOP = "02";

    private static final String OP = "op";
    private static final String DATA = "data";
    private static final String STATE = "st";
    private static final String OK_NEW_STATE = "okNewState";

    /**
     * 查询设备状态
     */
    @Override
    public Map<String, String> getDeviceState(String deviceNum) throws InterruptedException, IOException,
            MqttException {
        String response = publishByMqtt(deviceNum, STATE_QUERY);

        Map<String, String> status = null;
        JsonNode jsonNode = JsonUtils.readTree(response);
        String opValue = jsonNode.get(OP).asText();
        if (STATE.equals(opValue)) {
            String dataValue = jsonNode.get(DATA).toString();
            status = JsonUtils.readValue(dataValue, new TypeReference<Map<String, String>>() {
            });
        }

        return status;
    }

    @Override
    public CommonResponse startPath(String deviceNum, int pathId) throws MqttException, InterruptedException, IOException {
        String pathIdStr = pathId == 10 ? String.valueOf(pathId) : "0" + pathId;
        String timeStamp = "";
        String startCommand = "<<" + timeStamp + ":P" + pathIdStr + START + ">>";

        String response = publishByMqtt(deviceNum, startCommand);
        JsonNode jsonNode = JsonUtils.readTree(response);
        String opValue = jsonNode.get(OP).asText();

        //return OK_NEW_STATE.equals(opValue);
        return null;
    }

    @Override
    public CommonResponse stop(String deviceNum, int pathId) {
        return null;
    }


    /**
     * 处理 mqtt 回调
     *
     * @param json 设备响应的数据
     */
    public void mqttCallback(String json, String deviceNum) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        String op = jsonNode.get("op").asText();

    }

    private String publishByMqtt(String deviceNum, String message) throws MqttException, InterruptedException {
        return new BcMqttManager().publish(deviceNum, message);
    }
}
