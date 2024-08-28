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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminHomeControllerTest {
    @InjectMocks
    private AdminHomeController adminHomeController;

    @Mock
    private AdminService adminService;

    @Mock
    private ProductOrderService productOrderService;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

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
    void testGoToPage_WithValidSession() throws ParseException {
        when(session.getAttribute("adminId")).thenReturn("1");

        Admin admin = new Admin();
        admin.setAdmin_id(1);
        when(adminService.get(null, 1)).thenReturn(admin);
        when(productService.getTotal(any(), any(Byte[].class))).thenReturn(10);
        when(userService.getTotal(any())).thenReturn(5);
        when(productOrderService.getTotal(any(), any(Byte[].class))).thenReturn(7);

        String viewName = adminHomeController.goToPage(session, map);

        assertEquals("admin/homePage", viewName);
        assertNotNull(map.get("admin"));
        assertEquals(admin, map.get("admin"));
    }

    //测试用户未登录或 session 中没有 adminId 的情况下，goToPage 方法
    @Test
    void testGoToPage_WithInvalidSession() throws ParseException {
        when(session.getAttribute("adminId")).thenReturn(null);

        String viewName = adminHomeController.goToPage(session, map);

        assertEquals("redirect:/admin/login", viewName);
    }

    //测试在用户已登录的情况下，goToPageByAjax 方法
    @Test
    void testGoToPageByAjax_WithValidSession() throws ParseException {
        when(session.getAttribute("adminId")).thenReturn("1");

        Admin admin = new Admin();
        admin.setAdmin_id(1);
        when(adminService.get(null, 1)).thenReturn(admin);
        when(productService.getTotal(any(), any(Byte[].class))).thenReturn(10);
        when(userService.getTotal(any())).thenReturn(5);
        when(productOrderService.getTotal(any(), any(Byte[].class))).thenReturn(7);

        String viewName = adminHomeController.goToPageByAjax(session, map);

        assertEquals("admin/homeManagePage", viewName);
        assertNotNull(map.get("admin"));
        assertEquals(admin, map.get("admin"));
    }

    //测试用户未登录或 session 中没有 adminId 的情况下，goToPageByAjax 方法
    @Test
    void testGoToPageByAjax_WithInvalidSession() throws ParseException {
        when(session.getAttribute("adminId")).thenReturn(null);

        String viewName = adminHomeController.goToPageByAjax(session, map);

        assertEquals("admin/include/loginMessage", viewName);
    }

    //测试传入日期的情况下，getChartDataByDate 方法
    @Test
    void testGetChartDataByDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = sdf.parse("2024-08-20");
        Date endDate = sdf.parse("2024-08-27");

        JSONObject jsonResult = adminHomeController.getChartData(beginDate, endDate, 7);

        assertNotNull(jsonResult);
    }

    //测试不传入日期的情况下，getChartDataByDate 方法
    @Test
    void testGetChartDataByDate_WithoutDates() throws ParseException {
        JSONObject jsonResult = adminHomeController.getChartData(null, null, 7);

        assertNotNull(jsonResult);
    }

    //测试传入日期的情况下，getChartData 方法
    @Test
    void testGetChartData_ValidDates() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = sdf.parse("2024-08-20");
        Date endDate = sdf.parse("2024-08-27");

        JSONObject jsonResult = adminHomeController.getChartData(beginDate, endDate, 7);

        assertNotNull(jsonResult);
    }

    //测试传入日期的情况下，getChartData 方法
    @Test
    void testGetChartData_InvalidDates() throws ParseException {
        JSONObject jsonResult = adminHomeController.getChartData(null, null, 7);

        assertNotNull(jsonResult);
    }
}
