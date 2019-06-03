package com.superyc.heiniu.schedule;

import com.superyc.heiniu.schedule.job.ChargeStopJob;
import com.superyc.heiniu.schedule.job.StatusCheckJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class QuartzJobUtils {
    @Autowired
    public void setScheduler(Scheduler scheduler) {
        QuartzJobUtils.scheduler = scheduler;
    }

    private static Scheduler scheduler;
    private static final Logger logger = LoggerFactory.getLogger(QuartzJobUtils.class);


    /**
     * 设置定时关闭和状态查询任务
     */
    public static boolean startCharge(int chargeTime, int userId, String deviceNum, String path) {
        // 设置
        boolean setStopTime = setStopTime(chargeTime, userId, deviceNum, path);
        // 开始充电时，设置状态查询，迭代次数为1
        setStatusCheck(userId, deviceNum, path, 1);
        return setStopTime;
    }


    /**
     * 设置充电状态查询任务
     *
     * @param flag 状态查询标志位
     *             flag == -1, 表示持续查询时发现端口断电以后的断电确认查询
     *             flag == 0 时，表示持续查询
     *             1 <= flag <= 3 时，表示开始充电时的三次查询
     */
    private static void setStatusCheck(int userId, String deviceId, String path, int flag) {
        /*String lastJobBaseName = ChargeInfoBuffer.Instance.removeStatusCheckJob(userId);
        // 缓存中有上一次的检查任务，先清理掉再将本次任务添加进去
        if (lastJobBaseName != null && !"".equals(lastJobBaseName)) {
            stopJob(lastJobBaseName);
        }*/

        String baseName = userId + "-status check-";
        String jobName = "job" + baseName;
        String groupName = "group" + baseName;
        String triggerName = "trigger" + baseName;

        JobDetail jobDetail = JobBuilder.newJob(StatusCheckJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("userId", userId)
                .usingJobData("deviceId", deviceId)
                .usingJobData("path", path)
                .usingJobData("flag", flag)
                .build();

        int interval;
        // 根据 flag 设置任务时间，持续查询10分钟一次，初始查询1分钟一次, 断电确认隔一分钟后执行
        if (flag == 0) {
            interval = 10;
        } else {
            interval = 1;
        }

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, groupName)
                .startAt(DateBuilder.futureDate(interval, DateBuilder.IntervalUnit.MINUTE))
                .forJob(jobName, groupName)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
           // ChargeInfoBuffer.Instance.addStatusCheckJob(userId, baseName);
            logger.debug("设置状态查询任务：" + baseName + " flag:" + flag);
        } catch (SchedulerException e) {
            logger.error("quartz异常：" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 根据充电时长设置定时关闭
     */
    private static boolean setStopTime(int chargeTime, int userId, String deviceNum, String path) {
        // 设置定时关闭时间
        String baseName = userId + "-set stop time-";
        String jobName = "job" + baseName;
        String groupName = "group" + baseName;
        String triggerName = "trigger" + baseName;


        JobDetail jobDetail = JobBuilder.newJob(ChargeStopJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("clientId", userId)
                .usingJobData("deviceSn", deviceNum)
                .usingJobData("path", path)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, groupName)
                .startAt(DateBuilder.futureDate(chargeTime, DateBuilder.IntervalUnit.HOUR))
                .forJob(jobName, groupName)
                .build();

        try {
            logger.debug("充电时长：" + chargeTime);
            scheduler.scheduleJob(jobDetail, trigger);
            logger.debug("启动定时关闭" + baseName);
            return true;
        } catch (SchedulerException e) {
            logger.error("quartz异常：" + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 停止所有查询任务
     */
    private static void stopJob(String baseName) {
        String jobName = "job" + baseName;
        String groupName = "group" + baseName;
        String triggerName = "trigger" + baseName;

        try {
            scheduler.pauseTrigger(TriggerKey.triggerKey(triggerName, groupName));
            scheduler.pauseJob(JobKey.jobKey(jobName, groupName));
            scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName, groupName));
            scheduler.deleteJob(JobKey.jobKey(jobName, groupName));
        } catch (SchedulerException e) {
            logger.error("停止任务出错" + e.getMessage());
            e.printStackTrace();
        }
    }

    /*public static void  sustainStatusCheck(int clientId, String deviceSn, String path) {
        String baseName = clientId + "- sustain status check -" + new Date().getTime();
        String jobName = "job" + baseName;
        String groupName = "group" + baseName;
        String triggerName = "trigger" + baseName;

        JobDetail jobDetail = JobBuilder.newJob(StartStatusCheckJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("clientId", clientId)
                .usingJobData("deviceSn", deviceSn)
                .usingJobData("path", path)
                .build();

        //
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, groupName)
                .startAt(DateBuilder.futureDate(20, DateBuilder.IntervalUnit.SECOND))
                .forJob(jobName, groupName)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            logger.debug("持续初始状态查询" + baseName);
        } catch (SchedulerException e) {
            logger.error("quartz异常：" + e.getMessage());
            e.printStackTrace();
        }
    }*/
}
