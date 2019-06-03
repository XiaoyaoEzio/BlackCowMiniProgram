package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.ProxyClient;

public interface ProxyClientMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProxyClient record);

    int insertSelective(ProxyClient record);

    ProxyClient selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProxyClient record);

    int updateByPrimaryKey(ProxyClient record);
}