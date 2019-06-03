package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.SysParam;
import com.superyc.heiniu.bean.SysParamExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysParamMapper {
    long countByExample(SysParamExample example);

    int deleteByExample(SysParamExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SysParam record);

    int insertSelective(SysParam record);

    List<SysParam> selectByExample(SysParamExample example);

    SysParam selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SysParam record, @Param("example") SysParamExample example);

    int updateByExample(@Param("record") SysParam record, @Param("example") SysParamExample example);

    int updateByPrimaryKeySelective(SysParam record);

    int updateByPrimaryKey(SysParam record);
    
    String selectValueByName(String name);
}