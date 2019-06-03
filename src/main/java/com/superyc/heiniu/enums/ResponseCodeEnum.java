package com.superyc.heiniu.enums;

/**
 * 返回状态码
 */
public enum ResponseCodeEnum {
    ERROR(-1, "自定义错误"),

    OK(0, "成功"),
    BLANK_CODE(1001, "code为空"),
    INVALID_CODE(1002, "code无效"),
    BUSYNESS(1003, "系统繁忙，请重试"),
    FREQUENCY_LIMITATION(1004, "请求频率过高"),
    BLANK_SESSION(1005, "session为空"),
    EXPIRED_SESSION(1006, "session已过期"),
    INVALID_SESSION(1007, "session无效"),

    REGISTER_FAILURE(2001, "用户注册失败"),
    USER_NOT_FOUND(2002, "用户不存在"),

    WX_PAY_BLANK_SIGN(3001, "统一下单返回参数无签名"),
    WX_PAY_ERROR_SIGN(3002, "签名校验失败"),
    HAS_UNFINISHED_ORDER(3003, "存在未完成订单"),

    INSUFFICIENT_USER_BALANCE(4001, "用户余额不足"),
    INSUFFICIENT_GROUP_BALANCE(4002, "集团余额不足"),

    DEVICE_NOT_FOUND(5001, "设备不存在"),
    PANDA_CLOUD_ERROR(5002, "熊猫云返回值异常"),
    QUARTZ_START_ERROR(5003, "定时任务开启失败"),

    NOT_MATCHING(6001, "用户所有属集团与该设备不符"),


    TEST(999, "测试返回码");
    private String message;
    private int value;

    ResponseCodeEnum(int value, String message) {
        this.message = message;
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public int getValue() {
        return value;
    }
}
