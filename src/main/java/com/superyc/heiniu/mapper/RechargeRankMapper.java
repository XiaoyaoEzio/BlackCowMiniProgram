package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.RechargeRank;
import com.superyc.heiniu.bean.RechargeRankExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RechargeRankMapper {
    long countByExample(RechargeRankExample example);

    int deleteByExample(RechargeRankExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RechargeRank record);

    int insertSelective(RechargeRank record);

    List<RechargeRank> selectByExample(RechargeRankExample example);

    RechargeRank selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RechargeRank record, @Param("example") RechargeRankExample example);

    int updateByExample(@Param("record") RechargeRank record, @Param("example") RechargeRankExample example);

    int updateByPrimaryKeySelective(RechargeRank record);

    int updateByPrimaryKey(RechargeRank record);
    
    List<RechargeRank> selectAll();
}