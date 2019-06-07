package com.superyc.heiniu.schedule;

import com.superyc.heiniu.schedule.job.ChargeStopJob;
import com.superyc.heiniu.schedule.job.StatusCheckJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.quartz.DateBuilder.IntervalUnit;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Quartz任务处理
 */
@Component
public class QuartzJobUtils {
    private static final Logger LOG = LoggerFactory.getLogger(QuartzJobUtils.class);

    private static Scheduler scheduler;

    /**
     * 设置定时关闭和状态查询任务
     */
    public static boolean startCharge(int chargeTime, int userId, int deviceId, String deviceNum, int pathId) {
        // 设置定时关闭
        boolean setStopTime = setStopTime(chargeTime, userId, deviceId, deviceNum, pathId);
        // 设置端口状态查询
        setStatusCheck(chargeTime, userId, deviceId, deviceNum, pathId);
        return setStopTime;
    }


    /**
     * 设置充电状态查询任务
     * 开启充电后30s开始查询，每隔10分钟查询一次
     */
    private static void setStatusCheck(int chargeTime, int userId, int deviceId, String deviceNum, int pathId) {
        String baseName = getStatusCheckBaseName(userId);
        String jobName = "job" + baseName;
        String groupName = "group" + baseName;
        String triggerName = "trigger" + baseName;

        JobDetail jobDetail = JobBuilder.newJob(StatusCheckJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("userId", userId)
                .usingJobData("deviceId", deviceId)
                .usingJobData("deviceNum", deviceNum)
                .usingJobData("pathId", pathId)
                .build();

        Trigger trigger = newTrigger()
                .withIdentity(triggerName, groupName)
                .startAt(futureDate(30, IntervalUnit.SECOND))
                .withSchedule(
                        simpleSchedule()
                                .withIntervalInMinutes(10)
                                .withMisfireHandlingInstructionNextWithExistingCount()
                )
                .endAt(futureDate(chargeTime, IntervalUnit.HOUR))
                .forJob(jobName, groupName)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            LOG.debug("设置状态查询任务：" + baseName);
        } catch (SchedulerException e) {
            LOG.error("quartz异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据充电时长设置定时关闭
     */
    private static boolean setStopTime(int chargeTime, int userId, int deviceId, String deviceNum, int pathId) {
        // 设置定时关闭时间
        String baseName = getStopBaseName(userId);
        String jobName = "job-" + baseName;
        String groupName = "group-" + baseName;
        String triggerName = "trigger-" + baseName;


        JobDetail jobDetail = JobBuilder.newJob(ChargeStopJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("userId", userId)
                .usingJobData("deviceId", deviceId)
                .usingJobData("deviceNum", deviceNum)
                .usingJobData("pathId", pathId)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, groupName)
                .startAt(DateBuilder.futureDate(chargeTime, DateBuilder.IntervalUnit.HOUR))
                .forJob(jobName, groupName)
                .build();

        try {
            LOG.debug("设置定时关闭任务:{}, 充电时长:{}", baseName, chargeTime);
            scheduler.scheduleJob(jobDetail, trigger);
            return true;
        } catch (SchedulerException e) {
            LOG.error("quartz异常：" + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 停止所有查询任务
     */
    public static void stopJobs(int userId) {
        String[] baseNames = {getStatusCheckBaseName(userId), getStopBaseName(userId)};
        String jobName;
        String groupName;
        String triggerName;

        for (String baseName : baseNames) {
            jobName = "job-" + baseName;
            groupName = "group-" + baseName;
            triggerName = "trigger-" + baseName;
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, groupName);
            JobKey jobKey = JobKey.jobKey(jobName, groupName);
            try {
                scheduler.pauseTrigger(triggerKey);
                scheduler.pauseJob(jobKey);
                scheduler.unscheduleJob(triggerKey);
                scheduler.deleteJob(jobKey);
            } catch (SchedulerException e) {
                LOG.error("停止任务出错" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static String getStatusCheckBaseName(int userId) {
        return userId + "-status check";
    }

    private static String getStopBaseName(int userId) {
        return userId + "-set stop time";
    }

    @Autowired
    public void setScheduler(Scheduler scheduler) {
        QuartzJobUtils.scheduler = scheduler;
    }
}
