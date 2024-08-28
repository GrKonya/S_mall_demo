package com.xq.schoolmall.controller.admin;

import com.xq.schoolmall.entity.Admin;
import com.xq.schoolmall.service.AdminService;
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

class AccountControllerTest {
    @InjectMocks
    private AccountController accountController;

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

    //测试在用户已登录的情况下，goToPage 方法
    @Test
    void testGoToPage_WithValidSession() {
        when(session.getAttribute("adminId")).thenReturn("1");

        Admin admin = new Admin();
        admin.setAdmin_id(1);
        when(adminService.get(null, 1)).thenReturn(admin);

        String viewName = accountController.goToPage(session, map);

        assertEquals("admin/accountManagePage", viewName);
        assertNotNull(map.get("admin"));
        assertEquals(admin, map.get("admin"));
    }

    //测试在用户未登录或 session 中没有 adminId 的情况下，goToPage 方法
    @Test
    void testGoToPage_WithInvalidSession() {
        when(session.getAttribute("adminId")).thenReturn(null);

        String viewName = accountController.goToPage(session, map);

        assertEquals("admin/include/loginMessage", viewName);
    }

    //测试在用户已登录的情况下，logout 方法
    @Test
    void testLogout_WithValidSession() {
        when(session.getAttribute("adminId")).thenReturn("1");

        String viewName = accountController.logout(session);

        verify(session).removeAttribute("adminId");
        assertEquals("redirect:/admin/login", viewName);
    }

    //测试在用户未登录或 session 中没有 adminId 的情况下，logout 方法
    @Test
    void testLogout_WithInvalidSession() {
        when(session.getAttribute("adminId")).thenReturn(null);

        String viewName = accountController.logout(session);

        assertEquals("redirect:/admin/login", viewName);
    }

    //测试在用户请求更改昵称和密码时，updateAdmin 方法
    @Test
    void testUpdateAdmin_Success_WithPasswordChange() {
        when(session.getAttribute("adminId")).thenReturn("1");

        Admin admin = new Admin();
        admin.setAdmin_name("admin");

        when(adminService.get(null, 1)).thenReturn(admin);
        when(adminService.login("admin", "oldPass")).thenReturn(admin);
        when(adminService.update(any(Admin.class))).thenReturn(true);

        String jsonResponse = accountController.updateAdmin(session, "newNickname", "oldPass", "newPass", null, "1");

        assertTrue(jsonResponse.contains("\"success\":true"));
    }

    //测试当用户提供的旧密码不正确时，updateAdmin 方法
    @Test
    void testUpdateAdmin_Failure_InvalidOldPassword() {
        when(session.getAttribute("adminId")).thenReturn("1");

        Admin admin = new Admin();
        admin.setAdmin_name("admin");

        when(adminService.get(null, 1)).thenReturn(admin);
        when(adminService.login("admin", "wrongOldPass")).thenReturn(null);

        String jsonResponse = accountController.updateAdmin(session, "newNickname", "wrongOldPass", "newPass", null, "1");

        assertTrue(jsonResponse.contains("\"success\":false"));
        assertTrue(jsonResponse.contains("\"message\":\"原密码输入有误！\""));
    }
}
