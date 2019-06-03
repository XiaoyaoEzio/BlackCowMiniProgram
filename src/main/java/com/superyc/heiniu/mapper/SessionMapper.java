package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.Session;
import com.superyc.heiniu.bean.SessionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SessionMapper {
    long countByExample(SessionExample example);

    int deleteByExample(SessionExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Session record);

    int insertSelective(Session record);

    List<Session> selectByExample(SessionExample example);

    Session selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Session record, @Param("example") SessionExample example);

    int updateByExample(@Param("record") Session record, @Param("example") SessionExample example);

    int updateByPrimaryKeySelective(Session record);

    int updateByPrimaryKey(Session record);
    
    Session selectByUserId(Integer userId);

    Session selectBySign(String sign);
}