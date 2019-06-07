package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByUnionId(String unionId);

    String getMiniOpenIdByUserId(int id);

    int updateBalance(@Param("id") Integer id, @Param("alteration") int alteration);

    int getGroupId(int id);
}