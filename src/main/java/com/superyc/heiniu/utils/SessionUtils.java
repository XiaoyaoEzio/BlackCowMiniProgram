package com.superyc.heiniu.utils;

import com.superyc.heiniu.bean.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.Date;

/**
 * session相关工具
 */
@Component
public class SessionUtils {
    private static int sessionValidity;
    private static String sessionIdKey;
    private static String userIdKey;

    /**
     * 在响应头中设置session信息
     */
    public static void setSession(HttpServletResponse response, Session session) {
        response.setHeader(sessionIdKey, session.getSign());
    }

    /**
     * 生成一个uuid作为session的标识
     */
    public static String generateSign() {
        return RandomUtils.getUUID();
    }

    /**
     * 生成一个对应userId的session对象
     */
    public static Session generateSession(Integer userId) {
        Session session = new Session();
        session.setSign(SessionUtils.generateSign());
        session.setUserId(userId);
        session.setExpiredTime(SessionUtils.generateExpiredTime(sessionValidity));
        return session;
    }

    /**
     * 生成session的到期时间
     */
    public static Timestamp generateExpiredTime(int min) {
        return new Timestamp(System.currentTimeMillis() + min * 60 * 1000);
    }

    /**
     * 检查session是否过期
     *
     * @return true session已过期，false session有效
     */
    public static boolean isExpired(Session session) {
        Timestamp expiredTime = session.getExpiredTime();
        Timestamp currentTime = new Timestamp(new Date().getTime());
        return currentTime.after(expiredTime);
    }

    /**
     * 刷新session的标记和到期时间
     */
    public static void refreshSession(Session session) {
        session.setSign(SessionUtils.generateSign());
        session.setExpiredTime(SessionUtils.generateExpiredTime(sessionValidity));
    }

    @Value("#{session['sessionValidity']}")
    public void setSessionValidity(int sessionValidity) {
        SessionUtils.sessionValidity = sessionValidity;
    }

    @Value("#{session['sessionIdKey']}")
    public void setSessionIdKey(String sessionIdKey) {
        SessionUtils.sessionIdKey = sessionIdKey;
    }

    @Value("#{session['userIdKey']}")
    public void setUserIdKey(String userIdKey) {
        SessionUtils.userIdKey = userIdKey;
    }

    public static String getSessionIdKey() {
        return sessionIdKey;
    }

    public static String getUserIdKey() {
        return userIdKey;
    }

    public static int getSessionValidity() {
        return sessionValidity;
    }
}
