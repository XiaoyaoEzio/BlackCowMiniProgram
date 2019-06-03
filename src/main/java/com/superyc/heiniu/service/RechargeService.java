package com.superyc.heiniu.service;

import com.superyc.heiniu.api.wx.WxApi;
import com.superyc.heiniu.bean.CommonResponse;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * 充值相关业务
 */
public interface RechargeService {
    /**
     * 获取充值档位
     */
    CommonResponse getRechargeRank();

    /**
     * 充值
     */
    CommonResponse recharge(int userId, int rankId, String userIp) throws InvocationTargetException, NoSuchMethodException, JAXBException, IllegalAccessException, IOException;

    /**
     * 处理微信的回调请求
     * @param callbackResult
     */
    WxApi.AfterPayCallbackResponse callback(WxApi.AfterPayCallbackParam callbackResult) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, ParseException;

    /**
     * 获取充值记录
     */
    CommonResponse getRechargeList(int userId, int page, int size);
}
