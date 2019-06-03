package com.superyc.heiniu.params;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * session相关常量
 */
@Component
public class SessionParam {
    private static String sessionIdKey;
    private static String userIdKey;

    @Value("#{session['sessionIdKey']}")
    public void setSessionIdKey(String sessionIdKey) {
        SessionParam.sessionIdKey = sessionIdKey;
    }

    @Value("#{session['userIdKey']}")
    public void setUserIdKey(String userIdKey) {
        SessionParam.userIdKey = userIdKey;
    }

    public static String getSessionIdKey() {
        return sessionIdKey;
    }

    public static String getUserIdKey() {
        return userIdKey;
    }
}
