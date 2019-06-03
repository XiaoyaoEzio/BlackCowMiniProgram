package com.superyc.heiniu.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * MQTT协议工具类，声明为多例模式，为每个请求建立一个新的对象
 */
@Component
public class MqttUtils {
    private static String broker;
    private static String userName;
    private static String password;
    private static String serverTopic;
    private static String clientTopic;

    public static String getBroker() {
        return broker;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

    public static String getServerTopic() {
        return serverTopic;
    }

    public static String getClientTopic() {
        return clientTopic;
    }

    @Value("#{deviceMqtt['mqtt.clientTopic']}")
    public void setClientTopic(String clientTopic) {
        MqttUtils.clientTopic = clientTopic;
    }

    @Value("#{deviceMqtt['mqtt.serverTopic']}")
    public void setServerTopic(String serverTopic) {
        MqttUtils.serverTopic = serverTopic;
    }

    @Value("#{deviceMqtt['mqtt.broker']}")
    public void setBroker(String broker) {
        MqttUtils.broker = broker;
    }

    @Value("#{deviceMqtt['mqtt.username']}")
    public void setUserName(String userName) {
        MqttUtils.userName = userName;
    }

    @Value("#{deviceMqtt['mqtt.password']}")
    public void setPassword(String password) {
        MqttUtils.password = password;
    }
}
