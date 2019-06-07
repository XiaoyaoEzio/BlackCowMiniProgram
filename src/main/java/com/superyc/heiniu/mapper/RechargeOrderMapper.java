package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.RechargeOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RechargeOrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RechargeOrder record);

    int insertSelective(RechargeOrder record);

    RechargeOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RechargeOrder record);

    int updateByPrimaryKey(RechargeOrder record);

    RechargeOrder getUnfinishedOrder(int userId);

    RechargeOrder selectByOrderNumber(String orderNumber);

    List<RechargeOrder> getOrderList(@Param("userId") int userId, @Param("offset") int offset, @Param("size") int size);
}