package com.superyc.heiniu.mqtt;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于统一处理mqtt相关操作
 */
public class BcMqttManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private String response;

    private final String LOCK = "lock";

    /**
     * 发布消息并等待回复
     * @param message 要发布的消息
     * @return 回复信息
     */
    public String publish(String deviceNum, String message) throws MqttException, InterruptedException {
        BcMqttManager manager = this;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                BcMqttClient client = new BcMqttClient();
                try {
                    client.subscribe(deviceNum, manager);
                } catch (MqttException e) {
                    log.debug(e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        client.close();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t.start();
        Thread.sleep(2000);

        BcMqttServer server = new BcMqttServer();
        server.publish(message, deviceNum, this);

        synchronized (this) {
            while (StringUtils.isBlank(response)) {
                this.wait();
            }
        }

        return response;
    }

    public String getLOCK() {
        return LOCK;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
