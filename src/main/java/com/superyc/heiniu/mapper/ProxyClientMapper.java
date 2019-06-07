package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.ProxyClient;
import org.apache.ibatis.annotations.Param;

public interface ProxyClientMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProxyClient record);

    int insertSelective(ProxyClient record);

    ProxyClient selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProxyClient record);

    int updateByPrimaryKey(ProxyClient record);

    int updateBalance(@Param("id") Integer id, @Param("alteration") int alteration);
}