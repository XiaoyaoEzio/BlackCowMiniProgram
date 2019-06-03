package com.superyc.heiniu.enums;

public enum ProxyModeEnum {
    SELF_PROXY(1),
    LEVEL_PROXY(2),
    GROUP_PROXY(3)
    ;
    private int value;

    ProxyModeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
