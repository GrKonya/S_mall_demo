package com.xq.schoolmall.controller.fore;

import com.xq.schoolmall.controller.fore.ForeProductListController;
import com.xq.schoolmall.entity.Category;
import com.xq.schoolmall.entity.Product;
import com.xq.schoolmall.entity.User;
import com.xq.schoolmall.service.*;
import com.xq.schoolmall.util.PageUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ForeProductListControllerTest {

    @InjectMocks
    private ForeProductListController foreProductListController;

    @Mock
    private ProductService productService;
    @Mock
    private UserService userService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ProductImageService productImageService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private ProductOrderService productOrderService;
    @Mock
    private ProductOrderItemService productOrderItemService;
    @Mock
    private HttpSession session;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGoToPage_CategorySearch() {
        // 准备测试数据
        ModelMap map = new ModelMap();
        Integer categoryId = 1;
        Category category = new Category().setCategory_id(categoryId);
        Product product = new Product();
        product.setProduct_category(category);

        List<Product> productList = new ArrayList<>();
        productList.add(product);

        when(productService.getList(any(), any(), any(), any())).thenReturn(productList);
        when(productService.getTotal(any(), any())).thenReturn(1);
        when(categoryService.get(anyInt())).thenReturn(category);
        when(categoryService.getList(any(), any())).thenReturn(new ArrayList<>());

        // 调用方法
        String result = foreProductListController.goToPage(session, map, categoryId, null);

        // 验证结果
        assertEquals("fore/productListPage", result);
        verify(productService, times(1)).getList(any(), any(), any(), any());
    }

    @Test
    public void testGoToPage_NoCategoryOrProductName() {
        // 准备测试数据
        ModelMap map = new ModelMap();

        // 调用方法
        String result = foreProductListController.goToPage(session, map, null, null);

        // 验证结果
        assertEquals("redirect:/", result);
    }

    @Test
    public void testSearchProduct_ValidSearchWithMultipleKeywords() {
        // 准备测试数据
        ModelMap map = new ModelMap();
        Integer categoryId = 1;
        String productName = "test product";  // 多个关键词

        Category category = new Category().setCategory_id(categoryId);
        Product product = new Product();
        product.setProduct_category(category);

        List<Product> productList = new ArrayList<>();
        productList.add(product);

        // 模拟 getMoreList 和 getMoreListTotal 的调用
        when(productService.getMoreList(any(), any(), any(), any(), any())).thenReturn(productList);
        when(productService.getMoreListTotal(any(), any(), any())).thenReturn(1);
        when(categoryService.get(anyInt())).thenReturn(category);
        when(categoryService.getList(any(), any())).thenReturn(new ArrayList<>());

        // 调用方法
        String result = foreProductListController.searchProduct(session, map, 0, 20, categoryId, productName, null, true);

        // 验证结果
        assertEquals("fore/productListPage", result);
        verify(productService, times(1)).getMoreList(any(), any(), any(), any(), any());
        verify(productService, times(1)).getMoreListTotal(any(), any(), any());
    }

    @Test
    public void testSearchProduct_ValidSearchWithSingleKeyword() {
        // 准备测试数据
        ModelMap map = new ModelMap();
        Integer categoryId = 1;
        String productName = "test";  // 单个关键词

        Category category = new Category().setCategory_id(categoryId);
        Product product = new Product();
        product.setProduct_category(category);

        List<Product> productList = new ArrayList<>();
        productList.add(product);

        // 模拟 getList 和 getTotal 的调用
        when(productService.getList(any(), any(), any(), any())).thenReturn(productList);
        when(productService.getTotal(any(), any())).thenReturn(1);
        when(categoryService.get(anyInt())).thenReturn(category);
        when(categoryService.getList(any(), any())).thenReturn(new ArrayList<>());

        // 调用方法
        String result = foreProductListController.searchProduct(session, map, 0, 20, categoryId, productName, null, true);

        // 验证结果
        assertEquals("fore/productListPage", result);
        verify(productService, times(1)).getList(any(), any(), any(), any());
        verify(productService, times(1)).getTotal(any(), any());
    }

    @Test
    public void testSearchProduct_NoResults() {
        // 准备测试数据
        ModelMap map = new ModelMap();
        Integer categoryId = 1;
        String productName = "non-existent product";

        when(productService.getList(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        when(productService.getTotal(any(), any())).thenReturn(0);

        // 调用方法
        String result = foreProductListController.searchProduct(session, map, 0, 20, categoryId, productName, null, true);

        // 验证结果
        assertEquals("fore/productListPage", result);
        List<Product> productList = (List<Product>) map.get("productList");
        assertEquals(0, productList.size());
    }
}

