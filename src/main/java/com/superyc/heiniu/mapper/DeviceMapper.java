package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.Device;

public interface DeviceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Device record);

    int insertSelective(Device record);

    Device selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Device record);

    int updateByPrimaryKey(Device record);

    Device selectByDeviceNum(String deviceNum);

    String selectDeviceNumById(int deviceId);
}