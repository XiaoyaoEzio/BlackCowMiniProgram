package com.superyc.heiniu.utils;

import com.superyc.heiniu.mapper.SysParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class SysParamUtils {
    private static SysParamMapper sysParamMapper;

    /**
     * 获取客服电话
     */
    public static String getCustomerServiceTel() {
        return sysParamMapper.selectValueByName("customer_service_tel");
    }

    @Autowired
    public void setSysParamMapper(SysParamMapper sysParamMapper) {
        SysParamUtils.sysParamMapper = sysParamMapper;
    }
}
