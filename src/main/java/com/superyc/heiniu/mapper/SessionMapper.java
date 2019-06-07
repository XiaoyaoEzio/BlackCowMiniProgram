package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.Session;

public interface SessionMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Session record);

    int insertSelective(Session record);

    Session selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Session record);

    int updateByPrimaryKey(Session record);
}