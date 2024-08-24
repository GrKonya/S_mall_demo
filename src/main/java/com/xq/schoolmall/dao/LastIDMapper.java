package com.xq.schoolmall.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LastIDMapper {
    int selectLastID();
}
