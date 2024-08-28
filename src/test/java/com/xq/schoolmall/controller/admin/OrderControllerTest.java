package com.xq.schoolmall.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.xq.schoolmall.entity.Address;
import com.xq.schoolmall.entity.ProductOrder;
import com.xq.schoolmall.service.*;
import com.xq.schoolmall.util.OrderUtil;
import com.xq.schoolmall.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderControllerTest {
    @InjectMocks
    private OrderController orderController;

    @Mock
    private ProductOrderService productOrderService;

    @Mock
    private AddressService addressService;

    @Mock
    private ProductOrderItemService productOrderItemService;

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

    //测试正常情况下，goToPage方法
    @Test
    void testGoToPage_Success() {
        List<ProductOrder> orders = new ArrayList<>();
        when(productOrderService.getList(null, null, new OrderUtil("productOrder_id", true), new PageUtil(0, 10))).thenReturn(orders);
        when(productOrderService.getTotal(null, null)).thenReturn(100);
        String viewName = orderController.goToPage(session, map);

        assertEquals("admin/orderManagePage", viewName);
        assertEquals(orders, map.get("productOrderList"));
        assertEquals(100, map.get("productOrderCount"));
    }

    //测试异常情况下，goToPage方法
    @Test
    void testGoToPage_Failure() {
        when(productOrderService.getList(any(), any(), any(), any())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            orderController.goToPage(session, map);
        });
    }

    //测试正常情况下，goToDetailsPage方法
    @Test
    void testGoToDetailsPage_Success() {
        ProductOrder mockOrder = new ProductOrder();
        Address mockAddress;
        mockAddress =  addressService.get("110000");
        when(productOrderService.get(1)).thenReturn(mockOrder);
        when(addressService.get(String.valueOf(anyInt()))).thenReturn(mockAddress);
        when(productOrderItemService.getListByOrderId(anyInt(), any())).thenReturn(new ArrayList<>());

        String viewName = orderController.goToDetailsPage(null, map, 1);

        assertEquals("admin/include/orderDetails", viewName);
        assertNotNull(map.get("order"));
    }

    //测试异常情况下，goToDetailsPage方法
    @Test
    void testGoToDetailsPage_Failure() {
        int orderId = 1;
        when(productOrderService.get(orderId)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            orderController.goToDetailsPage(session, map, orderId);
        });
    }

    //测试订单更新成功时，updateOrder方法
    @Test
    void testUpdateOrder_Success() {
        String orderId = "1";
        when(productOrderService.update(any(ProductOrder.class))).thenReturn(true);

        String response = orderController.updateOrder(orderId);
        JSONObject jsonObject = JSONObject.parseObject(response);

        assertTrue(jsonObject.getBoolean("success"));
    }

    //测试订单更新失败时，updateOrder方法
    @Test
    void testUpdateOrder_Failure() {
        String orderId = "1";
        when(productOrderService.update(any(ProductOrder.class))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            orderController.updateOrder(orderId);
        });
    }

    //测试正常查询，getOrderBySearch方法
    @Test
    void testGetOrderBySearch_Success() {
        List<ProductOrder> mockOrderList = new ArrayList<>();
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_code("order123");
        mockOrderList.add(productOrder);

        when(productOrderService.getList(
                any(ProductOrder.class),
                any(Byte[].class),
                any(OrderUtil.class),
                any(PageUtil.class)
        )).thenReturn(mockOrderList);
        when(productOrderService.getTotal(any(ProductOrder.class), nullable(Byte[].class))).thenReturn(1);

        String result = orderController.getOrderBySearch("order123", "12345", new Byte[]{1}, null, true, 0, 10);

        // 验证返回结果
        String expectedJson = "{\"productOrderList\":[{\"productOrder_code\":\"order123\"}],\"totalPage\":1,\"pageUtil\":{\"count\":10,\"hasNext\":false,\"hasPrev\":false,\"index\":0,\"pageStart\":0,\"total\":1,\"totalPage\":1},\"productOrderCount\":1}";
        assertEquals(expectedJson, result);
    }

    //测试无查询结果，getOrderBySearch方法
    @Test
    void testGetOrderBySearch_Failure() {
        List<ProductOrder> mockOrderList = new ArrayList<>();
        when(productOrderService.getList(null, null, new OrderUtil("productOrder_id", true), new PageUtil(0, 10)))
                .thenReturn(mockOrderList);
        when(productOrderService.getTotal(null, null)).thenReturn(0);

        String result = orderController.getOrderBySearch("nonexistent", "00000", null, null, true, 0, 10);

        // 断言返回的 JSON 字符串符合预期
        String expectedJson = "{\"productOrderList\":[],\"totalPage\":0,\"pageUtil\":{\"count\":10,\"hasNext\":false,\"hasPrev\":false,\"index\":0,\"pageStart\":0,\"total\":0,\"totalPage\":0},\"productOrderCount\":0}";
        assertEquals(expectedJson, result);
    }
}
