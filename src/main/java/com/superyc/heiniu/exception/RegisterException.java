package com.superyc.heiniu.exception;

/**
 * 注册时异常
 */
public class RegisterException extends RuntimeException{
    public RegisterException() {
    }

    public RegisterException(String message) {
        super(message);
    }
}
