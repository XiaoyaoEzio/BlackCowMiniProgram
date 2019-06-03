package com.superyc.heiniu.mapper;

import com.superyc.heiniu.bean.ElectricityPrice;
import com.superyc.heiniu.bean.ElectricityPriceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ElectricityPriceMapper {
    long countByExample(ElectricityPriceExample example);

    int deleteByExample(ElectricityPriceExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ElectricityPrice record);

    int insertSelective(ElectricityPrice record);

    List<ElectricityPrice> selectByExample(ElectricityPriceExample example);

    ElectricityPrice selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ElectricityPrice record, @Param("example") ElectricityPriceExample example);

    int updateByExample(@Param("record") ElectricityPrice record, @Param("example") ElectricityPriceExample example);

    int updateByPrimaryKeySelective(ElectricityPrice record);

    int updateByPrimaryKey(ElectricityPrice record);

    int selectPriceByDeviceId(int deviceId);
}