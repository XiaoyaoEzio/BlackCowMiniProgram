package com.superyc.heiniu.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 订单流水号相关
 *      流水号格式  类型(1) + 订单生成时间(17) + 随机数(4)  共22位
 */
public class OrderNumberUtils {
    private static final String TIME_FORMAT = "yyyyMMddHHmmssSSS";
    private static final int RANDOM_BITS = 4;
    private static final String RECHARGE_TYPE = "1";
    private static final String CONSUME_TYPE = "2";
    private static final String TRANSFER_TYPE = "3";

    public static String getRechargeOrderNumber() {
        return RECHARGE_TYPE + getBaseOrderNumber();
    }

    public static String getConsumeOrderNumber() {
        return CONSUME_TYPE + getBaseOrderNumber();
    }

    public static String getTransferOrderNumber() { return TRANSFER_TYPE + getBaseOrderNumber();}

    public static int getOrderType(String orderNumber) {
        String typeString = orderNumber.substring(0, 1);
        if (RECHARGE_TYPE.equals(typeString)) {
            return new Integer(RECHARGE_TYPE);
        }

        if (CONSUME_TYPE.equals(typeString)) {
            return new Integer(CONSUME_TYPE);
        }

        return -1;
    }

    public static Date getOrderTime(String orderNumber) throws ParseException {
        String typeString = orderNumber.substring(1, 18);
        return new SimpleDateFormat(TIME_FORMAT).parse(typeString);
    }

    private static String getBaseOrderNumber() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT);
        String time = simpleDateFormat.format(new Date());
        return time + RandomUtils.getRandom(RANDOM_BITS);
    }
}
