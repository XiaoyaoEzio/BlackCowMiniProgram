package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.RechargeRank;

import java.util.List;

public interface RechargeRankMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RechargeRank record);

    int insertSelective(RechargeRank record);

    RechargeRank selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RechargeRank record);

    int updateByPrimaryKey(RechargeRank record);

    List<RechargeRank> selectAll();
}