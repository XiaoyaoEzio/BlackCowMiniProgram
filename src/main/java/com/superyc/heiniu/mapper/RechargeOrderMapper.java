package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.RechargeOrder;
import com.superyc.heiniu.bean.RechargeOrderExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RechargeOrderMapper {
    long countByExample(RechargeOrderExample example);

    int deleteByExample(RechargeOrderExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RechargeOrder record);

    int insertSelective(RechargeOrder record);

    List<RechargeOrder> selectByExample(RechargeOrderExample example);

    RechargeOrder selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RechargeOrder record, @Param("example") RechargeOrderExample example);

    int updateByExample(@Param("record") RechargeOrder record, @Param("example") RechargeOrderExample example);

    int updateByPrimaryKeySelective(RechargeOrder record);

    int updateByPrimaryKey(RechargeOrder record);

    RechargeOrder getUnfinishedOrder(int userId);

    RechargeOrder selectByOrderNumber(String orderNumber);

    List<RechargeOrder> getOrderList(@Param("userId") int userId,@Param("page") int page,@Param("size") int size);
}