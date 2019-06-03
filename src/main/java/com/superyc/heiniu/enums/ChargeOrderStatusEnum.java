package com.superyc.heiniu.enums;

public enum ChargeOrderStatusEnum {
    CHARGING(1),
    FINISHED(2)
    ;
    private int value;

    ChargeOrderStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
