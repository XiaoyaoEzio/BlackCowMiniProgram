package com.superyc.heiniu.exception;

/**
 * 熊猫云相关的异常
 */
public class PandaCloudException extends RuntimeException {
    public PandaCloudException() {
    }

    public PandaCloudException(String message) {
        super(message);
    }
}
