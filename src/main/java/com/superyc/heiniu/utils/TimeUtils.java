package com.superyc.heiniu.utils;

import org.springframework.stereotype.Component;

import java.util.Date;

/**
 *
 */
@Component
public class TimeUtils {
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static int getHours(Date startTime, Date endTime) {
        long start = startTime.getTime();
        long end = endTime.getTime();

        long interval = end - start;
        long ms = 1000L;
        long second = ms * 60;
        long minute = second * 60;
        long hour = minute * 60;

        Long result = interval / hour;
        return result.intValue();
    }
}
