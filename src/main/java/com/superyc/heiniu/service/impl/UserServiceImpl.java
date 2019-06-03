package com.superyc.heiniu.service.impl;

import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.bean.User;
import com.superyc.heiniu.mapper.UserMapper;
import com.superyc.heiniu.service.UserService;
import com.superyc.heiniu.utils.SysParamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class UserServiceImpl implements UserService {
    private UserMapper userMapper;
    @Override
    public CommonResponse getProfile(int userId) {
        User user = userMapper.selectByPrimaryKey(userId);

        ProfileParam profileParam = new ProfileParam();
        profileParam.setBalance(user.getBalance());
        profileParam.setTel(SysParamUtils.getCustomerServiceTel());

        return CommonResponse.success(profileParam);
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    private class ProfileParam {
        int balance;
        String tel;

        public int getBalance() {
            return balance;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }
    }
}
