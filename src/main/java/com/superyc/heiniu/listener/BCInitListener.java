package com.superyc.heiniu.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 初始化监听器
 */
@Component
public class BCInitListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // if (event.getApplicationContext().getParent() == null) {
        //
        // }
    }
}
