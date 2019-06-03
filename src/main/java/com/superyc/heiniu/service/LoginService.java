package com.superyc.heiniu.service;

import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.exception.RegisterException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public interface LoginService {
    CommonResponse login(String code, HttpServletResponse response) throws IOException, RegisterException;
}
