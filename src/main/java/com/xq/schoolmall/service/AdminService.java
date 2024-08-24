package com.xq.schoolmall.service;

import com.xq.schoolmall.entity.Admin;
import com.xq.schoolmall.util.PageUtil;

import java.util.List;

public interface AdminService {
    boolean add(Admin admin);
    boolean update(Admin admin);

    List<Admin> getList(String admin_name, PageUtil pageUtil);
    Admin get(String admin_name, Integer admin_id);
    Admin login(String admin_name, String admin_password);
    Integer getTotal(String admin_name);
}
