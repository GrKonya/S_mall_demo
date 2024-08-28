package com.xq.schoolmall.controller.fore;

import com.jayway.jsonpath.JsonPath;
import com.xq.schoolmall.entity.*;
import com.xq.schoolmall.service.*;
import com.xq.schoolmall.util.OrderUtil;
import com.xq.schoolmall.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ModelMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ForeOrderControllerTest {

    @InjectMocks
    private ForeOrderController foreOrderController;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductOrderService productOrderService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private ProductOrderItemService productOrderItemService;

    @Mock
    private AddressService addressService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private LastIDService lastIDService;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletRequest request;

    private ModelMap map;

    private User user;

    private Product product;

    private ProductOrder order;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        map = new ModelMap();

        user = new User();
        user.setUser_id(1);

        product = new Product();
        product.setProduct_id(1);
        product.setProduct_sale_price(100.0);
        Category category = new Category();
        category.setCategory_id(1);
        product.setProduct_category(category);

        order = new ProductOrder();
        order.setProductOrder_id(1);
        order.setProductOrder_code("123456");
        order.setProductOrder_status((byte) 0);
        order.setProductOrder_user(user);

        Address address = new Address();
        address.setAddress_areaId("110101");
        address.setAddress_name("Test District");

        Address region = new Address();
        region.setAddress_areaId("110100");
        region.setAddress_name("Test City");
        address.setAddress_regionId(region);
    }

    @Test
    public void testGoToPageSimple() {
        String result = foreOrderController.goToPageSimple();
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToPage_UserNotLoggedIn() {
        when(session.getAttribute("userId")).thenReturn(null);
        String result = foreOrderController.goToPage(session, new ModelMap(), null, 0, 10);
        assertEquals("redirect:/login", result);
    }

    @Test
    public void testGoToPage_UserLoggedIn_NoOrders() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getList(any(ProductOrder.class), any(Byte[].class), any(OrderUtil.class), any(PageUtil.class)))
                .thenReturn(new ArrayList<>());

        String result = foreOrderController.goToPage(session, new ModelMap(), null, 0, 10);
        assertEquals("fore/orderListPage", result);
    }

    @Test
    public void testGoToPage_UserLoggedIn_WithOrders() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);

        ProductOrder order = new ProductOrder();
        order.setProductOrder_id(1);
        order.setProductOrder_status((byte) 3);

        Product product = new Product();
        product.setProduct_id(1);

        ProductOrderItem orderItem = new ProductOrderItem();
        orderItem.setProductOrderItem_product(product);

        order.setProductOrderItemList(Arrays.asList(orderItem));

        List<ProductOrder> orderList = Arrays.asList(order);
        when(productOrderService.getList(any(ProductOrder.class), any(Byte[].class), any(OrderUtil.class), any(PageUtil.class)))
                .thenReturn(orderList);
        when(productOrderService.getTotal(any(ProductOrder.class), any(Byte[].class)))
                .thenReturn(1);

        when(productOrderItemService.getListByOrderId(anyInt(), any())).thenReturn(Arrays.asList(orderItem));
        when(productService.get(anyInt())).thenReturn(product);
        when(categoryService.getList(any(), any(PageUtil.class))).thenReturn(Arrays.asList(new Category()));
        when(reviewService.getTotalByOrderItemId(anyInt())).thenReturn(1);

        String result = foreOrderController.goToPage(session, new ModelMap(), null, 0, 10);
        assertEquals("fore/orderListPage", result);
    }

    @Test
    public void testGoToOrderConfirmPage_UserNotLoggedIn() throws UnsupportedEncodingException {
        when(session.getAttribute("userId")).thenReturn(null);
        String result = foreOrderController.goToOrderConfirmPage(1, (short) 1, new ModelMap(), session, request);
        assertEquals("redirect:/login", result);
    }

    @Test
    public void testGoToOrderConfirmPage_ProductNotFound() throws UnsupportedEncodingException {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productService.get(anyInt())).thenReturn(null);

        String result = foreOrderController.goToOrderConfirmPage(1, (short) 1, new ModelMap(), session, request);
        assertEquals("redirect:/", result);
    }

    @Test
    public void testGoToOrderConfirmPage_ProductFound() throws UnsupportedEncodingException {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productService.get(anyInt())).thenReturn(product);
        when(categoryService.get(anyInt())).thenReturn(new Category());
        when(productImageService.getList(anyInt(), anyByte(), any(PageUtil.class))).thenReturn(Arrays.asList(new ProductImage()));
        when(addressService.getRoot()).thenReturn(Arrays.asList(new Address()));
        when(addressService.getList(any(), anyString())).thenReturn(Arrays.asList(new Address()));

        Cookie[] cookies = new Cookie[]{
                new Cookie("addressId", "110000"),
                new Cookie("cityAddressId", "110100"),
                new Cookie("districtAddressId", "110101"),
                new Cookie("order_post", "110000"),
                new Cookie("order_receiver", "receiver"),
                new Cookie("order_phone", "123456789"),
                new Cookie("detailsAddress", "Test Address")
        };
        when(request.getCookies()).thenReturn(cookies);

        ModelMap map = new ModelMap();
        String result = foreOrderController.goToOrderConfirmPage(1, (short) 1, map, session, request);

        assertEquals("fore/productBuyPage", result);
        assertEquals(product.getProduct_sale_price() * 1, map.get("orderTotalPrice"));
        assertEquals("110000", map.get("addressId"));
        assertEquals("receiver", map.get("order_receiver"));
    }

    @Test
    public void testGoToOrderConfirmPageByCart_UserNotLoggedIn() throws UnsupportedEncodingException {
        when(session.getAttribute("userId")).thenReturn(null);
        String result = foreOrderController.goToOrderConfirmPageByCart(new ModelMap(), session, request, new Integer[]{1, 2});
        assertEquals("redirect:/login", result);
    }

    @Test
    public void testGoToOrderConfirmPageByCart_NoOrderItems() throws UnsupportedEncodingException {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);

        String result = foreOrderController.goToOrderConfirmPageByCart(new ModelMap(), session, request, null);
        assertEquals("redirect:/cart", result);
    }

    @Test
    public void testGoToOrderConfirmPageByCart_ValidOrderItems() throws UnsupportedEncodingException {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);

        // Create mock Product
        Product product = new Product();
        product.setProduct_id(1);
        product.setProduct_sale_price(100.0);

        // Create and set mock Category for the product
        Category category = new Category();
        category.setCategory_id(1);
        product.setProduct_category(category);

        // Create mock ProductOrderItem
        ProductOrderItem orderItem1 = new ProductOrderItem();
        orderItem1.setProductOrderItem_id(1);
        orderItem1.setProductOrderItem_price(100.0);
        orderItem1.setProductOrderItem_user(user);
        orderItem1.setProductOrderItem_product(product); // Set the product

        ProductOrderItem orderItem2 = new ProductOrderItem();
        orderItem2.setProductOrderItem_id(2);
        orderItem2.setProductOrderItem_price(200.0);
        orderItem2.setProductOrderItem_user(user);
        orderItem2.setProductOrderItem_product(product); // Set the product

        List<ProductOrderItem> orderItems = Arrays.asList(orderItem1, orderItem2);

        when(productOrderItemService.get(anyInt())).thenReturn(orderItem1).thenReturn(orderItem2);

        // Mock other service calls
        when(productService.get(anyInt())).thenReturn(product);
        when(categoryService.get(anyInt())).thenReturn(category);
        when(productImageService.getList(anyInt(), anyByte(), any(PageUtil.class)))
                .thenReturn(Arrays.asList(new ProductImage()));
        when(addressService.getRoot()).thenReturn(Arrays.asList(new Address()));
        when(addressService.getList(any(), anyString())).thenReturn(Arrays.asList(new Address()));

        // Mock cookies
        Cookie[] cookies = new Cookie[]{
                new Cookie("addressId", "110000"),
                new Cookie("cityAddressId", "110100"),
                new Cookie("districtAddressId", "110101"),
                new Cookie("order_post", "110000"),
                new Cookie("order_receiver", "receiver"),
                new Cookie("order_phone", "123456789"),
                new Cookie("detailsAddress", "Test Address")
        };
        when(request.getCookies()).thenReturn(cookies);

        ModelMap map = new ModelMap();
        String result = foreOrderController.goToOrderConfirmPageByCart(map, session, request, new Integer[]{1, 2});

        assertEquals("fore/productBuyPage", result);
        assertEquals(300.0, map.get("orderTotalPrice"));
        assertEquals("110000", map.get("addressId"));
        assertEquals("receiver", map.get("order_receiver"));
    }

    @Test
    public void testGoToOrderPayPage_UserNotLoggedIn() {
        when(session.getAttribute("userId")).thenReturn(null);

        String result = foreOrderController.goToOrderPayPage(new ModelMap(), session, "123456");
        assertEquals("redirect:/login", result);
    }

    @Test
    public void testGoToOrderPayPage_OrderNotFound() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(null);

        String result = foreOrderController.goToOrderPayPage(new ModelMap(), session, "123456");
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderPayPage_OrderStatusIncorrect() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);

        order.setProductOrder_status((byte) 1);  // Order status is not 0
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        String result = foreOrderController.goToOrderPayPage(new ModelMap(), session, "123456");
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderPayPage_ValidOrder() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        ProductOrderItem orderItem = new ProductOrderItem();
        orderItem.setProductOrderItem_price(100.0);

        Product product = new Product();
        product.setProduct_id(1);

        Category category = new Category();
        category.setCategory_id(1);

        product.setProduct_category(category);
        orderItem.setProductOrderItem_product(product);

        when(productOrderItemService.getListByOrderId(anyInt(), any())).thenReturn(Arrays.asList(orderItem));
        when(productService.get(anyInt())).thenReturn(product);

        ModelMap map = new ModelMap();
        String result = foreOrderController.goToOrderPayPage(map, session, "123456");

        assertEquals("fore/productPayPage", result);
        assertEquals(order, map.get("productOrder"));
        assertEquals(100.0, map.get("orderTotalPrice"));
    }

    @Test
    public void testGoToOrderPaySuccessPage_UserNotLoggedIn() {
        when(session.getAttribute("userId")).thenReturn(null);

        String result = foreOrderController.goToOrderPaySuccessPage(new ModelMap(), session, "123456");
        assertEquals("redirect:/login", result);
    }

    @Test
    public void testGoToOrderPaySuccessPage_OrderNotFound() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(null);

        String result = foreOrderController.goToOrderPaySuccessPage(new ModelMap(), session, "123456");
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderPaySuccessPage_OrderStatusIncorrect() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);

        order.setProductOrder_status((byte) 0);  // Status is not paid
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        String result = foreOrderController.goToOrderPaySuccessPage(new ModelMap(), session, "123456");
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderPaySuccessPage_ValidOrder() {
        // 模拟session中的用户ID
        when(session.getAttribute("userId")).thenReturn(1);
        // 模拟返回的用户对象
        User user = new User();
        user.setUser_id(1);
        when(userService.get(anyInt())).thenReturn(user);

        // 模拟订单对象
        ProductOrder order = new ProductOrder();
        order.setProductOrder_status((byte) 1); // 订单状态为已支付
        order.setProductOrder_user(user); // 订单的用户与session中的用户一致

        // 设置订单的地址信息
        Address orderAddress = new Address();
        orderAddress.setAddress_areaId("110101");
        order.setProductOrder_address(orderAddress);

        // 模拟通过订单号查询订单
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        // 模拟订单项
        ProductOrderItem orderItem = new ProductOrderItem();
        orderItem.setProductOrderItem_price(100.0);
        when(productOrderItemService.getListByOrderId(any(), eq(null))).thenReturn(Arrays.asList(orderItem));

        // 模拟地址服务
        Address address = new Address();
        address.setAddress_areaId("110101");
        address.setAddress_name("Test District");

        Address region = new Address();
        region.setAddress_areaId("110100");
        region.setAddress_name("Test City");
        address.setAddress_regionId(region);

        when(addressService.get(anyString())).thenReturn(address);

        // 执行测试方法
        ModelMap map = new ModelMap();
        String result = foreOrderController.goToOrderPaySuccessPage(map, session, "123456");

        // 验证结果
        assertEquals("fore/productPaySuccessPage", result);
        assertEquals(order, map.get("productOrder"));
        assertEquals(100.0, map.get("orderTotalPrice"));
    }

    @Test
    public void testGoToOrderConfirmPage_ValidOrder() {
        order.setProductOrder_status((byte)2);
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        ProductOrderItem orderItem = new ProductOrderItem();
        orderItem.setProductOrderItem_price(100.0);
        Product product = new Product();
        product.setProduct_id(1);
        orderItem.setProductOrderItem_product(product);

        when(productOrderItemService.getListByOrderId(anyInt(), any())).thenReturn(Arrays.asList(orderItem));
        when(productService.get(anyInt())).thenReturn(product);
        when(productImageService.getList(anyInt(), anyByte(), any(PageUtil.class))).thenReturn(Arrays.asList(new ProductImage()));

        String result = foreOrderController.goToOrderConfirmPage(map, session, "123456");

        assertEquals("fore/orderConfirmPage", result);
        assertEquals(order, map.get("productOrder"));
        assertEquals(100.0, map.get("orderTotalPrice"));
    }

    @Test
    public void testGoToOrderConfirmPage_InvalidOrder_NotLoggedIn() {
        when(session.getAttribute("userId")).thenReturn(null);

        String result = foreOrderController.goToOrderConfirmPage(map, session, "123456");

        assertEquals("redirect:/login", result);
    }

    @Test
    public void testGoToOrderConfirmPage_InvalidOrder_NotExist() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(null);

        String result = foreOrderController.goToOrderConfirmPage(map, session, "123456");

        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderConfirmPage_InvalidOrder_StatusNotConfirmed() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        order.setProductOrder_status((byte) 1); // Order status not confirmed
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        String result = foreOrderController.goToOrderConfirmPage(map, session, "123456");

        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderConfirmPage_InvalidOrder_UserMismatch() {
        when(session.getAttribute("userId")).thenReturn(2); // Different user ID
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        String result = foreOrderController.goToOrderConfirmPage(map, session, "123456");

        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderSuccessPage_ValidOrder() {
        order.setProductOrder_status((byte) 3);
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(order);
        when(productOrderItemService.getTotalByOrderId(anyInt())).thenReturn(1);

        ProductOrderItem orderItem = new ProductOrderItem();
        orderItem.setProductOrderItem_id(1);
        Product product = new Product();
        product.setProduct_id(1);
        orderItem.setProductOrderItem_product(product);

        when(productOrderItemService.getListByOrderId(anyInt(), any(PageUtil.class))).thenReturn(Arrays.asList(orderItem));
        when(reviewService.getTotalByOrderItemId(anyInt())).thenReturn(0);
        when(productService.get(anyInt())).thenReturn(product);
        when(productImageService.getList(anyInt(), anyByte(), any(PageUtil.class))).thenReturn(Arrays.asList(new ProductImage()));

        String result = foreOrderController.goToOrderSuccessPage(map, session, "123456");

        assertEquals("fore/orderSuccessPage", result);
        assertEquals(orderItem, map.get("orderItem"));
        assertEquals(product, map.get("product"));
    }

    @Test
    public void testGoToOrderSuccessPage_InvalidOrder_NotLoggedIn() {
        when(session.getAttribute("userId")).thenReturn(null);

        String result = foreOrderController.goToOrderSuccessPage(map, session, "123456");

        assertEquals("redirect:/login", result);
    }

    @Test
    public void testGoToOrderSuccessPage_InvalidOrder_NotExist() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(null);

        String result = foreOrderController.goToOrderSuccessPage(map, session, "123456");

        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderSuccessPage_InvalidOrder_StatusNotCompleted() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);
        order.setProductOrder_status((byte) 1); // Order status not completed
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        String result = foreOrderController.goToOrderSuccessPage(map, session, "123456");

        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToOrderSuccessPage_InvalidOrder_UserMismatch() {
        when(session.getAttribute("userId")).thenReturn(2); // Different user ID
        when(userService.get(anyInt())).thenReturn(user);
        when(productOrderService.getByCode(anyString())).thenReturn(order);

        String result = foreOrderController.goToOrderSuccessPage(map, session, "123456");

        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    public void testGoToCartPage_ValidUser() {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(anyInt())).thenReturn(user);

        // Mock the category
        Category category = new Category();
        category.setCategory_id(1);

        // Mock the product
        Product product = new Product();
        product.setProduct_id(1);
        product.setProduct_category(category);  // Set a non-null category

        // Mock the order item
        ProductOrderItem orderItem = new ProductOrderItem();
        orderItem.setProductOrderItem_product(product);

        List<ProductOrderItem> orderItemList = Arrays.asList(orderItem);

        when(productOrderItemService.getListByUserId(anyInt(), any())).thenReturn(orderItemList);
        when(productOrderItemService.getTotalByUserId(anyInt())).thenReturn(1);

        // Mock product-related services
        when(productService.get(anyInt())).thenReturn(product);
        when(productImageService.getList(anyInt(), anyByte(), any())).thenReturn(Arrays.asList(new ProductImage()));
        when(categoryService.get(anyInt())).thenReturn(category);

        String result = foreOrderController.goToCartPage(map, session);

        assertEquals("fore/productBuyCarPage", result);
        assertEquals(orderItemList, map.get("orderItemList"));
        assertEquals(1, map.get("orderItemTotal"));
    }

    @Test
    public void testGoToCartPage_NotLoggedIn() {
        when(session.getAttribute("userId")).thenReturn(null);

        String result = foreOrderController.goToCartPage(map, session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void testOrderDelivery_ValidOrder() {
        // Mock user session
        when(session.getAttribute("userId")).thenReturn(1);

        // Mock the order with status 1 (paid) and correct user ID
        ProductOrder order = new ProductOrder();
        order.setProductOrder_id(1);
        order.setProductOrder_code("123456");
        order.setProductOrder_status((byte) 1);
        User user = new User();
        user.setUser_id(1);
        order.setProductOrder_user(user);

        when(productOrderService.getByCode("123456")).thenReturn(order);

        // Test the method
        String result = foreOrderController.orderDelivery(session, "123456");

        // Verify the order status was updated
        verify(productOrderService, times(1)).update(any(ProductOrder.class));

        // Verify the redirection
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    void testOrderDelivery_OrderNotFound() {

        // Mock user session
        when(session.getAttribute("userId")).thenReturn(1);

        // Mock order not found
        when(productOrderService.getByCode("123456")).thenReturn(null);

        // Test the method
        String result = foreOrderController.orderDelivery(session, "123456");

        // Verify that the order was not updated
        verify(productOrderService, never()).update(any(ProductOrder.class));

        // Verify the redirection
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    void testOrderDelivery_InvalidOrderStatus() {

        // Mock user session
        when(session.getAttribute("userId")).thenReturn(1);

        // Mock the order with incorrect status
        ProductOrder order = new ProductOrder();
        order.setProductOrder_id(1);
        order.setProductOrder_code("123456");
        order.setProductOrder_status((byte) 0);  // Status not 1
        User user = new User();
        user.setUser_id(1);
        order.setProductOrder_user(user);

        when(productOrderService.getByCode("123456")).thenReturn(order);

        // Test the method
        String result = foreOrderController.orderDelivery(session, "123456");

        // Verify that the order was not updated
        verify(productOrderService, never()).update(any(ProductOrder.class));

        // Verify the redirection
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    void testOrderDelivery_UserNotLoggedIn() {

        // Mock user session
        when(session.getAttribute("userId")).thenReturn(null);  // User not logged in

        // Test the method
        String result = foreOrderController.orderDelivery(session, "123456");

        // Verify that the order was not updated
        verify(productOrderService, never()).update(any(ProductOrder.class));

        // Verify the redirection
        assertEquals("redirect:/order/0/10", result);
    }

    @Test
    void testOrderSuccess_ValidOrder() {

        // Mock user session
        when(session.getAttribute("userId")).thenReturn(1);

        // Mock the order with status 2 (delivered, awaiting confirmation)
        ProductOrder order = new ProductOrder();
        order.setProductOrder_id(1);
        order.setProductOrder_code("123456");
        order.setProductOrder_status((byte) 2);
        User user = new User();
        user.setUser_id(1);
        order.setProductOrder_user(user);

        when(productOrderService.getByCode("123456")).thenReturn(order);
        when(productOrderService.update(any(ProductOrder.class))).thenReturn(true);

        // Test the method
        String result = foreOrderController.orderSuccess(session, "123456");

        // Verify the order status was updated
        verify(productOrderService, times(1)).update(any(ProductOrder.class));

        // Assertions
        assertTrue(result.contains("\"success\":true"));
    }

    @Test
    void testOrderSuccess_OrderNotFound() {

        // Mock user session
        when(session.getAttribute("userId")).thenReturn(1);

        // Mock order not found
        when(productOrderService.getByCode("123456")).thenReturn(null);

        // Test the method
        String result = foreOrderController.orderSuccess(session, "123456");

        // Verify that the order was not updated
        verify(productOrderService, never()).update(any(ProductOrder.class));

        // Assertions
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("\"url\":\"/order/0/10\""));
    }

    @Test
    void testOrderSuccess_InvalidOrderStatus() {

        // Mock user session
        when(session.getAttribute("userId")).thenReturn(1);

        // Mock the order with incorrect status
        ProductOrder order = new ProductOrder();
        order.setProductOrder_id(1);
        order.setProductOrder_code("123456");
        order.setProductOrder_status((byte) 1);  // Status not 2
        User user = new User();
        user.setUser_id(1);
        order.setProductOrder_user(user);

        when(productOrderService.getByCode("123456")).thenReturn(order);

        // Test the method
        String result = foreOrderController.orderSuccess(session, "123456");

        // Verify that the order was not updated
        verify(productOrderService, never()).update(any(ProductOrder.class));

        // Assertions
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("\"url\":\"/order/0/10\""));
    }

    @Test
    void testOrderSuccess_UserNotLoggedIn() {

        // Mock user session
        when(session.getAttribute("userId")).thenReturn(null);  // User not logged in

        // Test the method
        String result = foreOrderController.orderSuccess(session, "123456");

        // Verify that the order was not updated
        verify(productOrderService, never()).update(any(ProductOrder.class));

        // Assertions
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("\"url\":\"/login\""));
    }

    @Test
    void testOrderClose_Success() {
        // Mock session user ID
        when(session.getAttribute("userId")).thenReturn(1);

        // Mock the order with status 0 (pending) and correct user ID
        ProductOrder order = new ProductOrder();
        order.setProductOrder_id(1);
        order.setProductOrder_code("123456");
        order.setProductOrder_status((byte) 0);
        User user = new User();
        user.setUser_id(1);
        order.setProductOrder_user(user);

        when(productOrderService.getByCode("123456")).thenReturn(order);
        when(productOrderService.update(any(ProductOrder.class))).thenReturn(true);

        // Test the method
        String result = foreOrderController.orderClose(session, "123456");

        JSONObject jsonObject = JSON.parseObject(result);
        assertTrue(jsonObject.getBoolean("success"));
    }

    @Test
    void testOrderClose_OrderNotFound() {
        // Mock session user ID
        when(session.getAttribute("userId")).thenReturn(1);

        // Mock order not found
        when(productOrderService.getByCode("123456")).thenReturn(null);

        // Test the method
        String result = foreOrderController.orderClose(session, "123456");

        JSONObject jsonObject = JSON.parseObject(result);
        assertFalse(jsonObject.getBoolean("success"));
        assertEquals("/order/0/10", JsonPath.read(result, "$.url"));
    }

    @Test
    public void testCreateOrderByOne_Success() throws Exception {
        when(foreOrderController.checkUser(session)).thenReturn(1);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 模拟返回的产品
        Product mockProduct = new Product();
        mockProduct.setProduct_id(1);
        mockProduct.setProduct_sale_price(100.0);
        when(productService.get(anyInt())).thenReturn(mockProduct);

        // 模拟添加订单成功
        when(productOrderService.add(any(ProductOrder.class))).thenReturn(true);
        when(productOrderItemService.add(any(ProductOrderItem.class))).thenReturn(true);

        // 模拟获取最后插入的订单ID
        when(lastIDService.selectLastID()).thenReturn(1);

        // 模拟请求参数
        String addressId = "110101";
        String cityAddressId = "110100";
        String districtAddressId = "110102";
        String productOrder_detail_address = "Some detail address";
        String productOrder_post = "000000";
        String productOrder_receiver = "Receiver";
        String productOrder_mobile = "1234567890";
        String userMessage = "User message";
        Integer orderItem_product_id = 1;
        Short orderItem_number = 2;

        // 调用控制器方法
        String result = foreOrderController.createOrderByOne(session, null, response,
                addressId, cityAddressId, districtAddressId, productOrder_detail_address,
                productOrder_post, productOrder_receiver, productOrder_mobile,
                userMessage, orderItem_product_id, orderItem_number);

        // 断言结果包含预期的内容
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"url\":\"/order/pay/"));
    }

    @Test
    public void testCreateOrderByOne_NotLoggedIn() throws Exception {
        // 创建响应
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 调用测试方法
        String result = foreOrderController.createOrderByOne(
                session, null, response, "110000", "110100", "110101",
                "Test Address", "123456", "Test Receiver", "13800138000", "Test Message", 1, (short) 1
        );

        // 验证结果
        assertEquals("{\"success\":false,\"url\":\"/login\"}", result, "用户未登录时，应该返回登录 URL");
    }

    @Test
    public void testCreateOrderByList_Success() throws UnsupportedEncodingException {
        when(session.getAttribute("userId")).thenReturn(1);
        MockHttpServletResponse response = new MockHttpServletResponse();
        // 模拟获取订单项
        ProductOrderItem mockOrderItem = new ProductOrderItem();
        mockOrderItem.setProductOrderItem_id(1);
        mockOrderItem.setProductOrderItem_user(new User().setUser_id(1));
        mockOrderItem.setProductOrderItem_product(new Product().setProduct_id(1));
        when(productOrderItemService.get(anyInt())).thenReturn(mockOrderItem);

        // 模拟产品服务返回的产品
        Product mockProduct = new Product();
        mockProduct.setProduct_id(1);
        when(productService.get(anyInt())).thenReturn(mockProduct);

        // 模拟订单服务添加订单成功
        when(productOrderService.add(any(ProductOrder.class))).thenReturn(true);

        // 模拟最后插入的订单ID
        when(lastIDService.selectLastID()).thenReturn(1);

        // 模拟更新订单项成功
        when(productOrderItemService.update(any(ProductOrderItem.class))).thenReturn(true);

        // 模拟请求参数
        String addressId = "110101";
        String cityAddressId = "110100";
        String districtAddressId = "110102";
        String productOrder_detail_address = "Some detail address";
        String productOrder_post = "000000";
        String productOrder_receiver = "Receiver";
        String productOrder_mobile = "1234567890";
        String orderItemJSON = "{\"1\":\"User message for item 1\"}";

        // 调用控制器方法
        String result = foreOrderController.createOrderByList(session, null, response,
                addressId, cityAddressId, districtAddressId, productOrder_detail_address,
                productOrder_post, productOrder_receiver, productOrder_mobile,
                orderItemJSON);

        // 断言结果包含预期的内容
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"url\":\"/order/pay/"));
    }

    @Test
    public void testCreateOrderItem_Success() {
        when(session.getAttribute("userId")).thenReturn(1);

        Product mockProduct = new Product();
        mockProduct.setProduct_id(1);
        mockProduct.setProduct_sale_price(100.0);
        when(productService.get(1)).thenReturn(mockProduct);

        ProductOrderItem existingOrderItem = new ProductOrderItem();
        existingOrderItem.setProductOrderItem_id(1);
        existingOrderItem.setProductOrderItem_product(mockProduct);
        existingOrderItem.setProductOrderItem_number((short) 1);
        existingOrderItem.setProductOrderItem_price(100.0);
        when(productOrderItemService.getListByUserId(1, null)).thenReturn(Arrays.asList(existingOrderItem));
        when(productOrderItemService.update(any(ProductOrderItem.class))).thenReturn(true);

        String result = foreOrderController.createOrderItem(1, (short) 1, session, request);

        assertTrue(result.contains("\"success\":true"));
    }

    @Test
    public void testCreateOrderItem_Fail_NotLoggedIn() {
        String result = foreOrderController.createOrderItem(1, (short) 1, session, request);
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("\"url\":\"/login\""));
    }

    @Test
    public void testCreateOrderItem_Fail_ProductNotFound() {
        when(session.getAttribute("userId")).thenReturn(1);

        when(productService.get(1)).thenReturn(null);

        String result = foreOrderController.createOrderItem(1, (short) 1, session, request);
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("\"url\":\"/login\""));
    }

    @Test
    public void testCreateOrderItem_Fail_AddNewOrderItem() {
        when(session.getAttribute("userId")).thenReturn(1);

        Product mockProduct = new Product();
        mockProduct.setProduct_id(1);
        mockProduct.setProduct_sale_price(100.0);
        when(productService.get(1)).thenReturn(mockProduct);

        when(productOrderItemService.getListByUserId(1, null)).thenReturn(Collections.emptyList());
        when(productOrderItemService.add(any(ProductOrderItem.class))).thenReturn(false);

        String result = foreOrderController.createOrderItem(1, (short) 1, session, request);
        assertTrue(result.contains("\"success\":false"));
    }

    @Test
    public void testDeleteOrderItem_Success() {
        when(session.getAttribute("userId")).thenReturn(1);

        ProductOrderItem mockOrderItem = new ProductOrderItem();
        mockOrderItem.setProductOrderItem_id(1);
        when(productOrderItemService.getListByUserId(1, null)).thenReturn(Arrays.asList(mockOrderItem));
        when(productOrderItemService.deleteList(any(Integer[].class))).thenReturn(true);

        String result = foreOrderController.deleteOrderItem(1, session, request);
        assertTrue(result.contains("\"success\":true"));
    }

    @Test
    public void testDeleteOrderItem_Fail_NotLoggedIn() {
        String result = foreOrderController.deleteOrderItem(1, session, request);
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("\"url\":\"/login\""));
    }

    @Test
    public void testDeleteOrderItem_Fail_OrderItemNotFound() {
        when(session.getAttribute("userId")).thenReturn(1);

        when(productOrderItemService.getListByUserId(1, null)).thenReturn(Collections.emptyList());

        String result = foreOrderController.deleteOrderItem(1, session, request);
        assertTrue(result.contains("\"success\":false"));
    }

    @Test
    public void testDeleteOrderItem_Fail_DeleteFailed() {
        when(session.getAttribute("userId")).thenReturn(1);

        ProductOrderItem mockOrderItem = new ProductOrderItem();
        mockOrderItem.setProductOrderItem_id(1);
        when(productOrderItemService.getListByUserId(1, null)).thenReturn(Arrays.asList(mockOrderItem));
        when(productOrderItemService.deleteList(any(Integer[].class))).thenReturn(false);

        String result = foreOrderController.deleteOrderItem(1, session, request);
        assertTrue(result.contains("\"success\":false"));
    }
}
