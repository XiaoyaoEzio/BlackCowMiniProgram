package com.superyc.heiniu.controller;

import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.exception.RegisterException;
import com.superyc.heiniu.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录控制器
 */
@Controller
@ResponseBody
public class LoginController {
    private Logger log = LoggerFactory.getLogger(LoginController.class);
    private LoginService loginService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public CommonResponse login(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException, RegisterException {
        if (StringUtils.isEmpty(code)) {
            return CommonResponse.failure(ResponseCodeEnum.BLANK_CODE);
        }
        log.info("code=" + code);
        return loginService.login(code, response);
    }

    @Autowired
    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

}
