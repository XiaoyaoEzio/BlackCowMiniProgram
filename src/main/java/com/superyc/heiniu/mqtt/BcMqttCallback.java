package com.superyc.heiniu.mqtt;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class BcMqttCallback implements MqttCallback {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private MqttClient mqttClient;
    private BcMqttManager manager;

    public BcMqttCallback() {
    }

    public BcMqttCallback(MqttClient mqttClient, BcMqttManager manager) {
        this.mqttClient = mqttClient;
        this.manager = manager;
    }


    @Override
    public void connectionLost(Throwable throwable) {
        log.info("mqtt connection lost. reconnect");
        while (true) {
            try {
                Thread.sleep(1000L);
                if (mqttClient != null) {
                    mqttClient.connect();
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("reconnect error");
                log.error("{}", ExceptionUtils.getStackTrace(e));
            }

        }
        log.info("reconnect complete");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        log.info("message arrive, {}", new String(mqttMessage.getPayload()));
        //if (MqttUtils.getClientTopic().equals(topic)) {
        //DeviceApi.mqttCallback(new String(mqttMessage.getPayload()), mqttClient.getClientId());
        //MqttUtils.getMessageMap().put(mqttClient.getClientId(), new String(mqttMessage.getPayload()));
        synchronized (manager) {
            manager.setResponse(new String(mqttMessage.getPayload()));
            manager.notify();
        }
        //}
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("delivery result:{}", iMqttDeliveryToken.isComplete());
    }

}
