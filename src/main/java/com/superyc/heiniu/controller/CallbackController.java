package com.superyc.heiniu.controller;

import com.superyc.heiniu.api.wx.WxApi;
import com.superyc.heiniu.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * 回调接口
 */
@Controller
@RequestMapping("/callback")
public class CallbackController {

    private RechargeService rechargeService;

    /**
     * 微信支付推送支付结果
     */
    @RequestMapping(value = "/pay", method = RequestMethod.POST, consumes = "application/xml", produces =
            "application/xml;charset=utf-8")
    @ResponseBody
    public WxApi.AfterPayCallbackResponse pay(@RequestBody WxApi.AfterPayCallbackParam callbackResult) throws InvocationTargetException, NoSuchMethodException, ParseException, IllegalAccessException {
        boolean b = WxApi.checkSign(callbackResult, WxApi.AfterPayCallbackParam.class);
        if (!b) {
            WxApi.AfterPayCallbackResponse response = new WxApi.AfterPayCallbackResponse();
            response.setReturnCode("FAIL");
            response.setReturnMsg("签名校验失败");
            return response;
        } else {
            WxApi.AfterPayCallbackResponse response = new WxApi.AfterPayCallbackResponse();
            response.setReturnCode("SUCCESS");
            response.setReturnMsg("成功");
            return response;
        }
        // TODO 测试
        //return rechargeService.callback(callbackResult);
    }

    @Autowired
    public void setRechargeService(RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }
}
