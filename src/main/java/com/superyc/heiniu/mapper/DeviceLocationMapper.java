package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.DeviceLocation;

public interface DeviceLocationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DeviceLocation record);

    int insertSelective(DeviceLocation record);

    DeviceLocation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DeviceLocation record);

    int updateByPrimaryKey(DeviceLocation record);
}