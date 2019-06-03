package com.superyc.heiniu.mqtt;

import com.superyc.heiniu.utils.MqttUtils;
import com.superyc.heiniu.utils.RandomUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class BcMqttClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private MqttClient client;

    public void subscribe(String deviceNum, BcMqttManager manager) throws MqttException {
        String clientId = "BCClient" + RandomUtils.getCurrentTime();
        // 连接服务器
        client = new MqttClient(MqttUtils.getBroker(), clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(MqttUtils.getUserName());
        options.setPassword(MqttUtils.getPassword().toCharArray());
        options.setKeepAliveInterval(120);

        //client.setCallback(new BcMqttCallback(client, manager));
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                System.out.println(new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        client.connect(options);

        String topic = MqttUtils.getClientTopic() + deviceNum;
        log.debug("topic: {}", topic);

        client.subscribe(topic);
    }

    public void close() throws MqttException {
        client.disconnect();
        client.close();
    }
}
