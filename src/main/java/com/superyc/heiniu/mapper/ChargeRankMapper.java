package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.ChargeRank;

import java.util.List;

public interface ChargeRankMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ChargeRank record);

    int insertSelective(ChargeRank record);

    ChargeRank selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ChargeRank record);

    int updateByPrimaryKey(ChargeRank record);

    List<ChargeRank> selectAll();
}