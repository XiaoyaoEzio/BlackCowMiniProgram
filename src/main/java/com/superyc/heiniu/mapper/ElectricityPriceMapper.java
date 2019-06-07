package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.ElectricityPrice;

public interface ElectricityPriceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ElectricityPrice record);

    int insertSelective(ElectricityPrice record);

    ElectricityPrice selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ElectricityPrice record);

    int updateByPrimaryKey(ElectricityPrice record);

    int selectPriceByDeviceId(int deviceId);
}