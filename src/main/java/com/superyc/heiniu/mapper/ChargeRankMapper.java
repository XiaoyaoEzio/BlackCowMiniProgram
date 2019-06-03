package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.ChargeRank;
import com.superyc.heiniu.bean.ChargeRankExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChargeRankMapper {
    long countByExample(ChargeRankExample example);

    int deleteByExample(ChargeRankExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ChargeRank record);

    int insertSelective(ChargeRank record);

    List<ChargeRank> selectByExample(ChargeRankExample example);

    ChargeRank selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ChargeRank record, @Param("example") ChargeRankExample example);

    int updateByExample(@Param("record") ChargeRank record, @Param("example") ChargeRankExample example);

    int updateByPrimaryKeySelective(ChargeRank record);

    int updateByPrimaryKey(ChargeRank record);
    
    List<ChargeRank> selectAll();
}