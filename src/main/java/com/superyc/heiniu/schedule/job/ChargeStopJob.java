package com.superyc.heiniu.schedule.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用来定时停止充电
 */
@Component
public class ChargeStopJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(ChargeStopJob.class);


    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        JobDataMap dataMap = jobContext.getJobDetail().getJobDataMap();
        int clientId = dataMap.getInt("userId");
        String deviceSn = dataMap.getString("deviceId");
        String path = dataMap.getString("path");

        logger.info("clientId：" + clientId);
        logger.info("deviceSn：" + deviceSn);
        logger.info("path：" + path);


        // TODO 停止设备
        logger.info("定时关闭端口");
    }
}
