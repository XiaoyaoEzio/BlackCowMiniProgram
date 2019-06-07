package com.superyc.heiniu.schedule.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 定时关闭端口
 */
@Component
public class ChargeStopJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(ChargeStopJob.class);

    private int userId;
    private int deviceId;
    private String  deviceNum;
    private int pathId;

    @Override
    public void execute(JobExecutionContext jobContext) {
        // TODO 停止设备
        LOG.info("定时关闭端口  userId:{}  deviceId:{}  pathId:{}", userId, deviceId, pathId);

    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceNum(String deviceNum) {
        this.deviceNum = deviceNum;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }
}
