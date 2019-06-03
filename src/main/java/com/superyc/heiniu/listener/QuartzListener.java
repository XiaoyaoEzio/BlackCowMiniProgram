package com.superyc.heiniu.listener;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 启动和停止Quartz的监听器
 */
@Component
public class QuartzListener implements ApplicationListener {
    private static final Logger logger = LoggerFactory.getLogger(QuartzListener.class);

    @Autowired
    private Scheduler scheduler;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (scheduler == null) {
            return;
        }

        if (event instanceof ContextRefreshedEvent) {
            // 判断是否为父容器调用，以解决事件被执行三次
            if (((ContextRefreshedEvent) event).getApplicationContext().getParent() == null) {
                try {
                    scheduler.start();
                    logger.info("启动quartz");
                } catch (SchedulerException e) {
                    logger.error("quartz启动异常：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        } /*else if (event instanceof ContextClosedEvent) {
            if (((ContextClosedEvent) event).getApplicationContext().getParent() == null) {
                try {
                    scheduler.shutdown();
                    logger.info("停止quartz");
                } catch (SchedulerException e) {
                    logger.error("quartz停止异常：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }*/

    }
}
