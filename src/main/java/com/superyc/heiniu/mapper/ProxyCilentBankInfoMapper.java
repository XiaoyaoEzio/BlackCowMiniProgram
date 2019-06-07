package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.ProxyCilentBankInfo;

public interface ProxyCilentBankInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProxyCilentBankInfo record);

    int insertSelective(ProxyCilentBankInfo record);

    ProxyCilentBankInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProxyCilentBankInfo record);

    int updateByPrimaryKey(ProxyCilentBankInfo record);
}