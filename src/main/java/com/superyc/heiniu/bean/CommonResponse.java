package com.superyc.heiniu.bean;

import com.superyc.heiniu.enums.ResponseCodeEnum;

import java.io.Serializable;

/**
 * 前后端交互通用Json对象
 */
public class CommonResponse implements Serializable {
    private Integer status;
    private String message;
    private Object data;

    public CommonResponse() {
    }

    public CommonResponse(Integer status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public CommonResponse(ResponseCodeEnum codeEnum, Object data) {
        this.status = codeEnum.getValue();
        this.message = codeEnum.getMessage();
        this.data = data;
    }

    public static boolean isSuccess(CommonResponse response) {
        return ResponseCodeEnum.OK.getValue() == response.getStatus();
    }

    public static CommonResponse success() {
        return success(null);
    }

    public static CommonResponse success(Object date) {
        return new CommonResponse(ResponseCodeEnum.OK, date);
    }

    public static CommonResponse failure(ResponseCodeEnum errCode) {
        return new CommonResponse(errCode, null);
    }

    public static CommonResponse failure(String msg) {
        return new CommonResponse(ResponseCodeEnum.ERROR.getValue(), msg, null);
    }

    public static CommonResponse failure(ResponseCodeEnum errCode, String msg) {
        return new CommonResponse(errCode.getValue(), msg, null);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
