package com.superyc.heiniu.mqtt;

import com.superyc.heiniu.utils.MqttUtils;
import com.superyc.heiniu.utils.TimeUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mqtt 服务端
 */
public class BcMqttServer {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void publish(String  message, String deviceNum, BcMqttManager manager) throws MqttException {
        final String CLIENT_ID = "BCServer" + TimeUtils.getCurrentTime();
        // 连接服务器
        MqttClient mqttClient = new MqttClient(MqttUtils.getBroker(), CLIENT_ID, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(MqttUtils.getUserName());
        options.setPassword(MqttUtils.getPassword().toCharArray());

        mqttClient.setCallback(new BcMqttCallback(mqttClient, manager));
        if (!mqttClient.isConnected()) {
            mqttClient.connect(options);
        }

        // 发布消息
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        mqttMessage.setQos(2);
        mqttClient.publish(MqttUtils.getServerTopic() + deviceNum, mqttMessage);

        log.info("publish message: {}", message);

        mqttClient.disconnect();
        mqttClient.close();
    }
}
