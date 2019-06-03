package com.superyc.heiniu.service.impl;

import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.bean.Session;
import com.superyc.heiniu.bean.User;
import com.superyc.heiniu.exception.RegisterException;
import com.superyc.heiniu.mapper.SessionMapper;
import com.superyc.heiniu.mapper.UserMapper;
import com.superyc.heiniu.service.LoginService;
import com.superyc.heiniu.utils.RandomUtils;
import com.superyc.heiniu.utils.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
@Service
public class LoginServiceImpl implements LoginService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private UserMapper userMapper;
    private SessionMapper sessionMapper;

    @Transactional
    @Override
    public CommonResponse login(String code, HttpServletResponse response) throws IOException, RegisterException {
        // TODO 测试登录与session
        /*WxApi.WxCode2SessionResult result = WxApi.code2Session(code);
        Integer errcode = result.getErrcode();

        // WxApi.ErrcodeEnum.SUCCESS == 0, 如果微信后台返回的errcode不为0，说明登录校验失败
        if (errcode != WxApi.ErrcodeEnum.SUCCESS.getCode()) {
            return CommonResponse.failure(WxApi.ErrcodeEnum.getResponseCodeEnum(errcode));
        }

        String unionId = result.getUnionid();
        String openId = result.getOpenid();*/

        String unionId = "123qwer";
        String openId = "openId123";
        User user = userMapper.selectByUnionId(unionId);

        // 用户不存在则创建用户
        if (user == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd" + RandomUtils.getRandom(4));
            String s = sdf.format(new Date());
            user = new User();
            user.setId(Integer.parseInt(s));
            user.setMiniOpenid(openId);
            user.setUnionid(unionId);
            user.setGroupId(0);
            user.setBalance(0);
            int value = userMapper.insert(user);
            if (value != 1) {
                throw new RegisterException("用户注册失败");
            }

            user = userMapper.selectByUnionId(unionId);
            log.info("用户注册成功, unionId: {}." , unionId);
        }

        Integer userId = user.getId();
        Session session = sessionMapper.selectByUserId(userId);

        if (session == null) {
            // session不存在则创建session
            sessionMapper.insert(SessionUtils.generateSession(userId));
            session = sessionMapper.selectByUserId(userId);
        } else {
            // session存在，刷新session
            SessionUtils.refreshSession(session);
            sessionMapper.updateByPrimaryKey(session);
        }

        SessionUtils.setSession(response, session);
        return CommonResponse.success();
    }


    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Autowired
    public void setSessionMapper(SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }
}
