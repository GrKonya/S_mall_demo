package com.xq.schoolmall.dao;

import com.xq.schoolmall.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {
    Integer insertOne(@Param("address") Address address);
    Integer updateOne(@Param("address") Address address);

    List<Address> select(@Param("address_name") String address_name, @Param("address_regionId") String address_regionId);
    Address selectOne(@Param("address_areaId") String address_areaId);
    List<Address> selectRoot();
}
