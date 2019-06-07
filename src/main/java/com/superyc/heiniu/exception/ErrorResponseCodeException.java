package com.superyc.heiniu.exception;

/**
 *
 */
public class ErrorResponseCodeException extends RuntimeException{
    public ErrorResponseCodeException() {
    }

    public ErrorResponseCodeException(String message) {
        super(message);
    }
}
