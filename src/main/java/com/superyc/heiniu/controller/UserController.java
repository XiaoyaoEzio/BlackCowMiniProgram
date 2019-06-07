package com.superyc.heiniu.controller;

import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.service.RechargeService;
import com.superyc.heiniu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * 用户相关控制器
 */
@Controller
@RequestMapping("/user")
@ResponseBody
public class UserController {
    private String userIdKey;
    private UserService userService;
    private RechargeService rechargeService;

    /**
     * 个人中心
     */
    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public CommonResponse getProfile(HttpServletRequest request) {
        int userId = (int) request.getAttribute(userIdKey);
        return userService.getProfile(userId);
    }

    /**
     * 获取充值档位
     */
    @RequestMapping(value = "/recharge/rank", method = RequestMethod.POST)
    public CommonResponse rechargeRank() {
        return rechargeService.getRechargeRank();
    }

    /**
     * 充值
     */
    @RequestMapping(value = "/recharge/handle", method = RequestMethod.POST)
    public CommonResponse recharge(HttpServletRequest request, @RequestParam("rank") int rankId) throws NoSuchMethodException, JAXBException, IllegalAccessException, InvocationTargetException, IOException {
        int userId = (int) request.getAttribute(userIdKey);
        String userIp = request.getRemoteAddr();
        return rechargeService.recharge(userId, rankId, userIp);
    }

    /**
     * 充值列表
     */
    @RequestMapping(value = "/recharge/list", method = RequestMethod.POST)
    public CommonResponse rechargeList(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            HttpServletRequest request) {
        // TODO 测试
        //int userId = (int) request.getAttribute(userIdKey);
        int userId = 1;
        return rechargeService.getRechargeList(userId, page, size);
    }

    @Value("#{session['userIdKey']}")
    public void setUserIdKey(String userIdKey) {
        this.userIdKey = userIdKey;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRechargeService(RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }
}
