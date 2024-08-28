package com.xq.schoolmall.controller.fore;

import com.alibaba.fastjson.JSONObject;
import com.xq.schoolmall.entity.Category;
import com.xq.schoolmall.entity.Product;
import com.xq.schoolmall.entity.User;
import com.xq.schoolmall.service.CategoryService;
import com.xq.schoolmall.service.ProductImageService;
import com.xq.schoolmall.service.ProductService;
import com.xq.schoolmall.service.UserService;
import com.xq.schoolmall.util.OrderUtil;
import com.xq.schoolmall.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ForeHomeControllerTest {

    @InjectMocks
    private ForeHomeController foreHomeController;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private HttpSession session;

    private MockMvc mockMvc;

    @Mock
    private Map<String, Object> map;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(foreHomeController).build();
    }

    //测试用户已登录时，goToPage方法
    @Test
    void testGoToPage_UserLoggedIn() throws Exception {
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.get(1)).thenReturn(new User());

        List<Category> categoryList = new ArrayList<>();
        when(categoryService.getList(any(), any())).thenReturn(categoryList);

        List<Product> productList = new ArrayList<>();
        when(productService.getList(any(Product.class), any(Byte[].class), any(OrderUtil.class), any(PageUtil.class)))
                .thenReturn(productList);

        String viewName = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getModelAndView()
                .getViewName();

        assertEquals("fore/homePage", viewName);
    }

    //测试用户未登录时，goToPage方法
    @Test
    void testGoToPage_UserNotLoggedIn() throws Exception {
        when(session.getAttribute("userId")).thenReturn(null);

        List<Category> categoryList = new ArrayList<>();
        when(categoryService.getList(any(), any())).thenReturn(categoryList);

        List<Product> productList = new ArrayList<>();
        when(productService.getList(any(Product.class), any(Byte[].class), any(OrderUtil.class), any(PageUtil.class)))
                .thenReturn(productList);

        String viewName = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getModelAndView()
                .getViewName();

        assertEquals("fore/homePage", viewName);
    }

    //测试转到错误页面时，goToErrorPage方法
    @Test
    void testGoToErrorPage() throws Exception {
        String viewName = mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andReturn()
                .getModelAndView()
                .getViewName();

        assertEquals("fore/errorPage", viewName);
    }

    //测试有效 category_id 时，getProductByNav方法
    @Test
    void testGetProductByNav_Success() throws Exception {
        List<Product> productList = new ArrayList<>(Arrays.asList(new Product(), new Product()));
        when(productService.getTitle(any(Product.class), any(PageUtil.class))).thenReturn(productList);

        String result = mockMvc.perform(get("/product/nav/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject expectedObject = new JSONObject();
        expectedObject.put("success", true);
        expectedObject.put("category", new Category().setCategory_id(1));

        assertEquals(expectedObject.getString("success"), JSONObject.parseObject(result).getString("success"));
    }

    // 测试无效的 category_id 时，getProductByNav方法
    @Test
    void testGetProductByNav_Failure() throws Exception {
        String result = mockMvc.perform(get("/product/nav/99999"))//定义99999为无效id
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject expectedObject = new JSONObject();
        expectedObject.put("success", false);

        assertEquals(expectedObject.toJSONString(), result);
    }
}