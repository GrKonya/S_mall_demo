package com.xq.schoolmall.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class BaseControllerTest {

    @InjectMocks
    private BaseController baseController;

    @Mock
    private HttpSession session;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testCheckAdmin_LoggedIn() {
        // 模拟管理员登录
        Integer adminId = 1;
        when(session.getAttribute("adminId")).thenReturn(adminId);

        // 调用方法
        Object result = baseController.checkAdmin(session);

        // 验证结果
        assertEquals(adminId, result);
    }

    @Test
    public void testCheckAdmin_NotLoggedIn() {
        // 模拟管理员未登录
        when(session.getAttribute("adminId")).thenReturn(null);

        // 调用方法
        Object result = baseController.checkAdmin(session);

        // 验证结果
        assertNull(result);
    }

    @Test
    public void testCheckUser_LoggedIn() {
        // 模拟用户登录
        Integer userId = 1;
        when(session.getAttribute("userId")).thenReturn(userId);

        // 调用方法
        Object result = baseController.checkUser(session);

        // 验证结果
        assertEquals(userId, result);
    }

    @Test
    public void testCheckUser_NotLoggedIn() {
        // 模拟用户未登录
        when(session.getAttribute("userId")).thenReturn(null);

        // 调用方法
        Object result = baseController.checkUser(session);

        // 验证结果
        assertNull(result);
    }
}
