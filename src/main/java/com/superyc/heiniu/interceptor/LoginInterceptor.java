package com.superyc.heiniu.interceptor;

import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.bean.Session;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.mapper.SessionMapper;
import com.superyc.heiniu.params.SessionParam;
import com.superyc.heiniu.utils.JsonUtils;
import com.superyc.heiniu.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 登录拦截器，验证session信息并在合适的情况下刷新session
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private SessionMapper sessionMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String sessionSign = request.getHeader(SessionParam.getSessionIdKey());

        if (StringUtils.isEmpty(sessionSign)) {
            response(response, ResponseCodeEnum.BLANK_SESSION);
            return false;
        }

        Session session = sessionMapper.selectBySign(sessionSign);
        if (session == null) {
            response(response, ResponseCodeEnum.INVALID_SESSION);
            return false;
        }

        if (SessionUtils.isExpired(session)) {
            response(response, ResponseCodeEnum.EXPIRED_SESSION);
            return false;
        }

        // session存在且有效，放行，并刷新session
        request.setAttribute(SessionParam.getUserIdKey(), session.getUserId());
        SessionUtils.refreshSession(session);
        sessionMapper.updateByPrimaryKey(session);
        response.setHeader(SessionParam.getSessionIdKey(), session.getSign());

        return true;
    }

    private void response(HttpServletResponse response, ResponseCodeEnum codeEnum) throws IOException {
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        PrintWriter pw = response.getWriter();

        pw.write(JsonUtils.getString(CommonResponse.failure(codeEnum)));
        pw.flush();
        pw.close();
    }

    @Autowired
    public void setSessionMapper(SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }
}
