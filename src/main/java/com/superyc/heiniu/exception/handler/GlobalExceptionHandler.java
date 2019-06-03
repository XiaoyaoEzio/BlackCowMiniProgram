package com.superyc.heiniu.exception.handler;

import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.exception.RegisterException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler{
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(RegisterException.class)
    public CommonResponse registerException(RegisterException ex) {
        log.error("用户注册失败\n{}", ExceptionUtils.getStackTrace(ex));

        return CommonResponse.failure(ResponseCodeEnum.REGISTER_FAILURE);
    }
}
