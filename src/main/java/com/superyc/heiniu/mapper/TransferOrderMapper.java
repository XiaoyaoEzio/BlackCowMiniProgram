package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.TransferOrder;

public interface TransferOrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TransferOrder record);

    int insertSelective(TransferOrder record);

    TransferOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TransferOrder record);

    int updateByPrimaryKey(TransferOrder record);
}