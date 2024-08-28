package com.xq.schoolmall.controller.fore;

import com.alibaba.fastjson.JSONObject;
import com.xq.schoolmall.controller.fore.ForeReviewController;
import com.xq.schoolmall.entity.*;
import com.xq.schoolmall.service.*;
import com.xq.schoolmall.util.PageUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpSession;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class ForeReviewControllerTest {

    @InjectMocks
    private ForeReviewController foreReviewController;

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private ProductOrderItemService productOrderItemService;

    @Mock
    private ProductOrderService productOrderService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private HttpSession session;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGoToPage_Success() {
        // 准备测试数据
        ModelMap map = new ModelMap();
        Integer orderItemId = 1;
        Integer userId = 1;
        Integer productOrderId = 1;
        Integer productId = 100;

        // Mock 用户
        User user = new User().setUser_id(userId);

        // Mock 订单项及其相关订单
        ProductOrder productOrder = new ProductOrder().setProductOrder_id(productOrderId).setProductOrder_status((byte) 3);
        Product product = new Product().setProduct_id(productId);
        ProductOrderItem orderItem = new ProductOrderItem()
                .setProductOrderItem_user(user)
                .setProductOrderItem_product(product)
                .setProductOrderItem_order(productOrder);

        // Mock session 和服务调用
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.get(userId)).thenReturn(user);
        when(productOrderItemService.get(orderItemId)).thenReturn(orderItem);
        when(reviewService.getTotalByOrderItemId(orderItemId)).thenReturn(0);
        when(productOrderService.get(productOrderId)).thenReturn(productOrder);
        when(productService.get(productId)).thenReturn(product);
        when(productImageService.getList(eq(productId), eq((byte) 0), any())).thenReturn(new ArrayList<>());

        // 调用方法
        String result = foreReviewController.goToPage(session, map, orderItemId);

        // 验证结果
        assertEquals("fore/addReview", result);
        verify(userService, times(1)).get(userId);
        verify(productOrderItemService, times(1)).get(orderItemId);
        verify(productOrderService, times(1)).get(productOrderId);
        verify(reviewService, times(1)).getTotalByOrderItemId(orderItemId);
        verify(productService, times(1)).get(productId);
        verify(productImageService, times(1)).getList(eq(productId), eq((byte) 0), any());
    }

    @Test
    public void testGoToPage_OrderItemUserMismatch() {
        // 准备测试数据
        ModelMap map = new ModelMap();
        Integer orderItemId = 1;
        Integer userId = 1;
        Integer differentUserId = 2;

        // Mock 用户
        User user = new User().setUser_id(differentUserId);  // 与 session 中的 userId 不匹配

        // Mock 订单项
        ProductOrderItem orderItem = new ProductOrderItem()
                .setProductOrderItem_user(user)
                .setProductOrderItem_order(new ProductOrder().setProductOrder_id(1).setProductOrder_status((byte) 3));

        // Mock session 和服务调用
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.get(userId)).thenReturn(new User().setUser_id(userId));
        when(productOrderItemService.get(orderItemId)).thenReturn(orderItem);

        // 调用方法
        String result = foreReviewController.goToPage(session, map, orderItemId);

        // 验证结果
        assertEquals("redirect:/order/0/10", result);
        verify(productOrderItemService, times(1)).get(orderItemId);
        verify(userService, times(1)).get(userId);
        verify(reviewService, never()).getTotalByOrderItemId(orderItemId);
        verify(productService, never()).get(anyInt());
        verify(productImageService, never()).getList(anyInt(), eq((byte) 0), any());
    }

    @Test
    public void testAddReview_Success() throws Exception {
        // 准备测试数据
        Integer orderItemId = 1;
        String reviewContent = "Great product!";
        Integer userId = 1;
        Integer productOrderId = 1;
        Integer productId = 100;

        User user = new User().setUser_id(userId);

        ProductOrder productOrder = new ProductOrder().setProductOrder_id(productOrderId).setProductOrder_status((byte) 3);
        Product product = new Product().setProduct_id(productId).setProduct_review_count(10); // 设置初始评论数

        ProductOrderItem orderItem = new ProductOrderItem()
                .setProductOrderItem_user(user)
                .setProductOrderItem_product(product)
                .setProductOrderItem_order(productOrder);

        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.get(userId)).thenReturn(user);
        when(productOrderItemService.get(orderItemId)).thenReturn(orderItem);
        when(productOrderService.get(productOrderId)).thenReturn(productOrder);
        when(reviewService.getTotalByOrderItemId(orderItemId)).thenReturn(0);
        when(reviewService.add(any(Review.class))).thenReturn(true);
        when(productService.get(productId)).thenReturn(product);
        when(productService.update(any(Product.class))).thenReturn(true);

        Map<String, Object> map = new HashMap<>();

        String result = foreReviewController.addReview(session, map, orderItemId, reviewContent);

        assertEquals("redirect:/product/" + productId, result);
        verify(reviewService, times(1)).add(any(Review.class));
        verify(productService, times(1)).update(any(Product.class));
    }

    @Test
    public void testAddReview_OrderIsNull() throws Exception {
        // 准备测试数据
        Integer orderItemId = 1;
        String reviewContent = "Great product!";
        Integer userId = 1;

        // 创建用户对象
        User user = new User().setUser_id(userId);

        // 创建 Product 对象
        Product product = new Product().setProduct_id(100);

        // 创建订单项但不设置订单（即订单为 null）
        ProductOrderItem orderItem = new ProductOrderItem()
                .setProductOrderItem_user(user)
                .setProductOrderItem_product(product)
                .setProductOrderItem_order(null);  // 模拟订单为 null 的情况

        // Mock session 和服务调用
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.get(userId)).thenReturn(user);
        when(productOrderItemService.get(orderItemId)).thenReturn(orderItem);

        // 创建一个非空的 Map 对象
        Map<String, Object> map = new HashMap<>();

        // 调用方法
        String result = foreReviewController.addReview(session, map, orderItemId, reviewContent);

        // 验证结果
        assertEquals("redirect:/order/0/10", result);
        verify(reviewService, never()).add(any(Review.class));
        verify(productService, never()).update(any(Product.class));
    }

    @Test
    public void testGetReviewInfo_Success() {
        // 准备测试数据
        Integer productId = 1;
        Integer index = 0;
        Integer count = 10;

        // 创建 mock 的评论列表
        List<Review> reviewList = new ArrayList<>();
        User user = new User().setUser_id(1).setUser_name("Test User");
        Review review = new Review().setReview_user(user);
        reviewList.add(review);

        // 模拟 reviewService 和 userService 的行为
        when(reviewService.getListByProductId(eq(productId), any(PageUtil.class))).thenReturn(reviewList);
        when(userService.get(1)).thenReturn(user);
        when(reviewService.getTotalByProductId(eq(productId))).thenReturn(1);

        // 调用方法
        String result = foreReviewController.getReviewInfo(productId, index, count);

        // 解析结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        List<Review> resultList = jsonObject.getJSONArray("reviewList").toJavaList(Review.class);
        PageUtil pageUtil = jsonObject.getObject("pageUtil", PageUtil.class);

        // 验证结果
        assertEquals(1, resultList.size());
        assertEquals(1, (int) pageUtil.getTotal());
        assertEquals(Optional.of(index), Optional.of((int) pageUtil.getIndex()));
        assertEquals(Optional.of(count), Optional.of((int) pageUtil.getCount()));
        verify(reviewService, times(1)).getListByProductId(eq(productId), any(PageUtil.class));
        verify(userService, times(1)).get(1);
    }

    @Test
    public void testGetReviewInfo_ReviewUserIsNull() {
        // 准备测试数据
        Integer productId = 1;
        Integer index = 0;
        Integer count = 10;

        // 创建 mock 的评论列表，其中用户信息为 null
        List<Review> reviewList = new ArrayList<>();
        Review review = new Review().setReview_user(null);  // 模拟用户信息为 null 的情况
        reviewList.add(review);

        // 模拟 reviewService 的行为
        when(reviewService.getListByProductId(eq(productId), any(PageUtil.class))).thenReturn(reviewList);
        when(reviewService.getTotalByProductId(eq(productId))).thenReturn(1);

        // 调用方法
        String result = foreReviewController.getReviewInfo(productId, index, count);

        // 解析结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        List<Review> resultList = jsonObject.getJSONArray("reviewList").toJavaList(Review.class);
        PageUtil pageUtil = jsonObject.getObject("pageUtil", PageUtil.class);

        // 验证结果
        assertEquals(1, resultList.size());
        assertNull(resultList.get(0).getReview_user());  // 用户信息应为 null
        assertEquals(1, (int) pageUtil.getTotal());
        verify(reviewService, times(1)).getListByProductId(eq(productId), any(PageUtil.class));
        verify(reviewService, times(1)).getTotalByProductId(eq(productId));
        verify(userService, never()).get(anyInt());  // 不应调用 userService，因为用户信息为 null
    }



}
