package com.xq.schoolmall.controller.fore;

import com.xq.schoolmall.entity.*;
import com.xq.schoolmall.service.*;
import com.xq.schoolmall.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ForeProductDetailsControllerTest {

    @InjectMocks
    private ForeProductDetailsController foreProductDetailsController;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PropertyValueService propertyValueService;

    @Mock
    private PropertyService propertyService;

    @Mock
    private ReviewService reviewService;

    private MockHttpSession session;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private MockMvc mockMvc;

    private Map<String, Object> map;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        map = new HashMap<>();
    }

    @Test
    public void testGoToPage_Success() {
        // 模拟 session 中的用户 ID
        session.setAttribute("userId", 1);

        // 模拟产品对象及其属性
        Product product = new Product();
        product.setProduct_id(1);
        product.setProduct_isEnabled((byte) 0);
        Category category = new Category();
        category.setCategory_id(1);
        product.setProduct_category(category);

        // 模拟返回的列表
        List<ProductImage> singleProductImageList = Collections.singletonList(new ProductImage());
        List<ProductImage> detailsProductImageList = Collections.singletonList(new ProductImage());
        List<PropertyValue> propertyValueList = Collections.singletonList(new PropertyValue().setPropertyValue_property(new Property().setProperty_id(1)));
        List<Property> propertyList = Collections.singletonList(new Property().setProperty_id(1));
        List<Review> reviewList = Collections.singletonList(new Review().setReview_user(new User().setUser_id(1)));

        // 模拟服务返回的对象和数据
        when(productService.get(1)).thenReturn(product);
        when(categoryService.get(1)).thenReturn(category);
        when(productImageService.getList(1, (byte) 0, null)).thenReturn(singleProductImageList);
        when(productImageService.getList(1, (byte) 1, null)).thenReturn(detailsProductImageList);
        when(propertyValueService.getList(any(), any())).thenReturn(propertyValueList);
        when(propertyService.getList(any(), any())).thenReturn(propertyList);
        when(reviewService.getListByProductId(1, null)).thenReturn(reviewList);
        when(userService.get(1)).thenReturn(new User().setUser_id(1));
        when(productService.getTotal(any(), any())).thenReturn(5);
        when(productService.getList(any(), any(), any(), any())).thenReturn(Collections.singletonList(new Product()));

        // 执行测试
        String result = foreProductDetailsController.goToPage(session, map, "1");

        // 验证结果
        assertEquals("fore/productDetailsPage", result);
        assertTrue(map.containsKey("product"));
        assertTrue(map.containsKey("loveProductList"));
        assertTrue(map.containsKey("categoryList"));
    }

    @Test
    public void testGoToPage_ProductNotFound() {
        // 模拟产品对象不存在或被禁用的情况
        when(productService.get(anyInt())).thenReturn(null);  // 模拟产品不存在
        String result = foreProductDetailsController.goToPage(session, map, "1");
        assertEquals("redirect:/404", result);

        // 或者，产品被禁用的情况
        Product disabledProduct = new Product();
        disabledProduct.setProduct_isEnabled((byte) 1);
        when(productService.get(anyInt())).thenReturn(disabledProduct);  // 模拟产品被禁用
        result = foreProductDetailsController.goToPage(session, map, "1");
        assertEquals("redirect:/404", result);
    }

    @Test
    public void testLoadProductReviewList_Success() {
        // 设置测试数据
        int productId = 1;
        int index = 1;
        int count = 10;

        // 模拟服务返回的评论列表
        Review review = new Review();
        review.setReview_id(1);
        review.setReview_content("Great product!");
        List<Review> reviewList = Collections.singletonList(review);

        // 模拟 reviewService 返回值
        when(reviewService.getListByProductId(eq(productId), any(PageUtil.class))).thenReturn(reviewList);

        // 执行测试
        String result = foreProductDetailsController.loadProductReviewList(String.valueOf(productId), index, count);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("\"review_id\":1"));
        assertTrue(result.contains("\"review_content\":\"Great product!\""));
    }

    @Test
    public void testLoadProductReviewList_InvalidProductId() {
        // 设置无效的产品ID
        String invalidProductId = "999"; // 假设999是无效的产品ID
        int index = 1;
        int count = 10;

        // 模拟 reviewService 返回空列表
        when(reviewService.getListByProductId(anyInt(), any(PageUtil.class))).thenReturn(Collections.emptyList());

        // 执行测试
        String result = foreProductDetailsController.loadProductReviewList(invalidProductId, index, count);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("\"reviewList\":[]"));
    }

    @Test
    public void testLoadProductPropertyList_Success() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(foreProductDetailsController).build();

        // 准备测试数据
        Integer productId = 1;

        PropertyValue propertyValue = new PropertyValue();
        Property property = new Property();
        property.setProperty_id(1);
        propertyValue.setPropertyValue_property(property);
        List<PropertyValue> propertyValueList = Arrays.asList(propertyValue);

        Property property1 = new Property();
        property1.setProperty_id(1);
        List<Property> propertyList = Arrays.asList(property1);

        when(propertyValueService.getList(any(PropertyValue.class), isNull())).thenReturn(propertyValueList);
        when(propertyService.getList(any(Property.class), isNull())).thenReturn(propertyList);

        // 执行测试方法
        String result = foreProductDetailsController.loadProductPropertyList(String.valueOf(productId));

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("\"propertyList\""));
        assertTrue(result.contains("\"property_id\":1"));
    }

    @Test
    public void testLoadProductPropertyList_Failure() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(foreProductDetailsController).build();

        // 准备测试数据
        Integer productId = 1;

        when(propertyValueService.getList(any(PropertyValue.class), isNull())).thenReturn(Collections.emptyList());
        when(propertyService.getList(any(Property.class), isNull())).thenReturn(Collections.emptyList());

        // 执行测试方法
        String result = foreProductDetailsController.loadProductPropertyList(String.valueOf(productId));

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("\"propertyList\""));
        assertFalse(result.contains("\"property_id\":1"));
    }

    @Test
    public void testGuessYouLike_Success() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(foreProductDetailsController).build();

        // 准备测试数据
        Integer cid = 1;
        Integer guessNumber = 0;
        int total = 10;

        // 模拟服务层返回的产品列表
        Product product1 = new Product();
        product1.setProduct_id(1);
        Product product2 = new Product();
        product2.setProduct_id(2);
        List<Product> loveProductList = Arrays.asList(product1, product2);

        when(productService.getTotal(any(Product.class), any(Byte[].class))).thenReturn(total);
        when(productService.getList(any(Product.class), any(Byte[].class), isNull(), any(PageUtil.class)))
                .thenReturn(loveProductList);

        // 执行测试方法
        String result = foreProductDetailsController.guessYouLike(cid, guessNumber);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"loveProductList\""));
        assertTrue(result.contains("\"guessNumber\""));
    }

    @Test
    public void testGuessYouLike_Failure() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(foreProductDetailsController).build();

        // 准备测试数据
        Integer cid = 1;
        Integer guessNumber = 0;
        int total = 0;  // 设置 total 为 0，这样会引发 IllegalArgumentException

        // 模拟服务层返回总数为 0
        when(productService.getTotal(any(Product.class), any(Byte[].class))).thenReturn(total);

        // 模拟服务层返回空的产品列表
        when(productService.getList(any(Product.class), any(Byte[].class), isNull(), any(PageUtil.class)))
                .thenReturn(Collections.emptyList());

        // 执行测试方法并捕获异常
        assertThrows(IllegalArgumentException.class, () -> {
            foreProductDetailsController.guessYouLike(cid, guessNumber);
        });
    }
}
