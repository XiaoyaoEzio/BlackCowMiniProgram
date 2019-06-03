package com.superyc.heiniu.enums;

/**
 * 充电支付模式
 */
public enum ChargePayMode {
    USER(1),
    GROUP(2)
    ;

    private int value;

    public int getValue() {
        return value;
    }

    ChargePayMode(int value) {
        this.value = value;
    }}
