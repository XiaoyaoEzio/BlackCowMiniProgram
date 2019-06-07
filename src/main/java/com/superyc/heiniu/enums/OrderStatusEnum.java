package com.superyc.heiniu.enums;

public enum OrderStatusEnum {
    DEALING(1),
    FINISHED(2),
    CANCEL(3)
    ;


    OrderStatusEnum(int value) {
        this.value = value;
    }

    int value;
    public int getValue() {
        return value;
    }
}
