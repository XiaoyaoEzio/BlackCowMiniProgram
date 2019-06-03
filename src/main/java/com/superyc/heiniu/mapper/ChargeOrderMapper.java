package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.ChargeOrder;

public interface ChargeOrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ChargeOrder record);

    int insertSelective(ChargeOrder record);

    ChargeOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ChargeOrder record);

    int updateByPrimaryKey(ChargeOrder record);

    ChargeOrder selectFirstUnfinishedOrderByUserId(Integer userId);
}