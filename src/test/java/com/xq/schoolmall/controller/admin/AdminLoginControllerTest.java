package com.xq.schoolmall.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.xq.schoolmall.entity.Admin;
import com.xq.schoolmall.service.AdminService;
import com.xq.schoolmall.service.ProductOrderService;
import com.xq.schoolmall.service.ProductService;
import com.xq.schoolmall.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminLoginControllerTest {
    @InjectMocks
    private AdminLoginController adminLoginController;

    @Mock
    private AdminService adminService;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private Map<String, Object> map;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        map = new HashMap<>();
    }

    @Test
    void testGoToPage() {
        String viewName = adminLoginController.goToPage();
        assertEquals("admin/loginPage", viewName);
    }

    //测试登录成功的情况，checkLogin方法
    @Test
    void testCheckLogin_Success() {
        Admin admin = new Admin();
        admin.setAdmin_id(1);
        when(adminService.login("validUsername", "validPassword")).thenReturn(admin);

        String jsonResponse = adminLoginController.checkLogin(session, "validUsername", "validPassword");

        verify(session).setAttribute("adminId", 1);
        JSONObject responseObject = JSONObject.parseObject(jsonResponse);
        assertTrue(responseObject.getBoolean("success"));
    }

    //测试登录失败的情况，checkLogin方法
    @Test
    void testCheckLogin_Failure() {
        when(adminService.login("invalidUsername", "invalidPassword")).thenReturn(null);

        String jsonResponse = adminLoginController.checkLogin(session, "invalidUsername", "invalidPassword");

        JSONObject responseObject = JSONObject.parseObject(jsonResponse);
        assertFalse(responseObject.getBoolean("success"));
    }
}
