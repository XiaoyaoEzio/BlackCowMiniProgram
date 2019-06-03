package com.superyc.heiniu.utils;

import java.util.Random;
import java.util.UUID;

/**
 * 随机数
 */
public class RandomUtils {
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String getRandom(int bit) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bit; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
