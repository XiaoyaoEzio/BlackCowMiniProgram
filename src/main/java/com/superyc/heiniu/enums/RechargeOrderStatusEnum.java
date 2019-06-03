package com.superyc.heiniu.enums;

/**
 * 订单状态
 */
public enum RechargeOrderStatusEnum {
    /**
     * 等待支付
     */
    WAITING_PAY(1),
    /**
     * 完成支付
     */
    FINISH_PAY(2),
    /**
     * 取消订单
     */
    CANCEL(3);


    private int value;

    RechargeOrderStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }}
