package com.xq.schoolmall.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.schoolmall.entity.*;
import com.xq.schoolmall.service.*;
import com.xq.schoolmall.util.OrderUtil;
import com.xq.schoolmall.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductControllerTest {
    @InjectMocks
    private ProductController productController;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductOrderService productOrderService;

    @Mock
    private AddressService addressService;

    @Mock
    private UserService userService;

    @Mock
    private ProductOrderItemService productOrderItemService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private PropertyService propertyService;

    @Mock
    private PropertyValueService propertyValueService;

    @Mock
    private LastIDService lastIDService;

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
        List<Category> categoryList = new ArrayList<>();
        List<Product> productList = new ArrayList<>();

        when(categoryService.getList(null, null)).thenReturn(categoryList);
        when(productService.getList(null, null, null, new PageUtil(1, 10))).thenReturn(productList);
        when(productService.getTotal(null, null)).thenReturn(1);

        String viewName = productController.goToPage(session, map);

        assertEquals(categoryList, map.get("categoryList"));
        assertEquals(productList, map.get("productList"));
        assertEquals(1, map.get("productCount"));
        assertTrue(map.containsKey("pageUtil"));
        assertEquals("admin/productManagePage", viewName);
    }

    //测试异常情况下，goToPage方法
    @Test
    void testGoToPage_Failure() {
        when(categoryService.getList(null, null)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            productController.goToPage(session, map);
        });
    }

    //测试正常情况下，goToDetailsPage方法
    @Test
    void testGoToDetailsPage_Success() {
        int pid = 1;
        Product product = new Product();
        when(productService.get(pid)).thenReturn(product);
        when(productImageService.getList(anyInt(), anyByte(), any())).thenReturn(Collections.emptyList());
        when(propertyValueService.getList(any(), any())).thenReturn(Collections.emptyList());
        when(propertyService.getList(any(), any())).thenReturn(Collections.emptyList());
        List<Category> categories = Collections.singletonList(new Category());
        when(categoryService.getList(null, null)).thenReturn(categories);

        String viewName = productController.goToDetailsPage(session, map, pid);

        assertEquals("admin/include/productDetails", viewName);
        assertEquals(product, map.get("product"));
        assertTrue(map.containsKey("propertyList"));
        assertEquals(categories, map.get("categoryList"));
    }

    //测试异常情况下，goToDetailsPage方法
    @Test
    void testGoToDetailsPage_Failure() {
        Integer pid = 1;
        when(productService.get(pid)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            productController.goToDetailsPage(session, map, pid);
        });
    }

    //测试正常情况下，goToAddPage方法
    @Test
    void testGoToAddPage_Success(){
        List<Category> categoryList = Arrays.asList(new Category().setCategory_id(1));
        List<Property> propertyList = new ArrayList<>();
        when(categoryService.getList(null,null)).thenReturn(categoryList);
        when(propertyService.getList(any(Property.class), eq(null))).thenReturn(propertyList);

        String viewName = productController.goToAddPage(session, map);

        assertEquals("admin/include/productDetails", viewName);
        assertEquals(categoryList, map.get("categoryList"));
        assertEquals(propertyList, map.get("propertyList"));
    }

    //测试异常情况下，goToAddPage方法
    @Test
    void testGoToAddPage_Failure(){
        when(categoryService.getList(null, null)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            productController.goToAddPage(session, map);
        });
    }

    // 测试正常情况下，addProduct 方法
    @Test
    public void testAddProduct_Success() {
        String productJson = "{\"1\":\"value1\"}";
        Product product = new Product().setProduct_id(1);
        when(productService.add(any())).thenReturn(true);
        when(lastIDService.selectLastID()).thenReturn(1);
        when(propertyValueService.addList(any())).thenReturn(true);
        when(productImageService.addList(any())).thenReturn(true);

        String response = productController.addProduct(
                "productName", "productTitle", 1, 100.0, 150.0, (byte) 1,
                productJson, new String[]{"image1.jpg"}, new String[]{"detail1.jpg"}
        );

        JSONObject jsonObject = JSONObject.parseObject(response);
        assertTrue(jsonObject.getBoolean("success"));
        assertEquals(Optional.of(1), Optional.ofNullable(jsonObject.getInteger("product_id")));

    }

    // 测试异常情况下，addProduct 方法
    @Test
    public void testAddProduct_Failure() {
        // 准备
        String productJson = "{\"1\":\"value1\"}";
        when(productService.add(any())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            productController.addProduct(
                    "productName", "productTitle", 1, 100.0, 150.0, (byte) 1,
                    productJson, new String[]{"image1.jpg"}, new String[]{"detail1.jpg"}
            );
        });
    }

    //测试正常情况下，updateProduct 方法
    @Test
    public void testUpdateProduct_Success() {
        // 准备
        String propertyAddJson = "{\"1\":\"value1\"}";
        String propertyUpdateJson = "{\"2\":\"value2\"}";
        Product product = new Product().setProduct_id(1);
        when(productService.update(any())).thenReturn(true);
        when(propertyValueService.addList(any())).thenReturn(true);
        when(propertyValueService.update(any())).thenReturn(true);
        when(propertyValueService.deleteList(any())).thenReturn(true);
        when(productImageService.addList(any())).thenReturn(true);

        String response = productController.updateProduct(
                "productName", "productTitle", 1, 100.0, 150.0, (byte) 1,
                propertyAddJson, propertyUpdateJson, new Integer[]{1},
                new String[]{"image1.jpg"}, new String[]{"detail1.jpg"}, 1
        );

        JSONObject jsonObject = JSONObject.parseObject(response);
        assertTrue(jsonObject.getBoolean("success"));
        assertEquals(Optional.of(1), Optional.ofNullable(jsonObject.getInteger("product_id")));
    }

    //测试正常情况下，updateProduct 方法
    @Test
    public void testUpdateProduct_Failure() {
        String propertyAddJson = "{\"1\":\"value1\"}";
        String propertyUpdateJson = "{\"2\":\"value2\"}";
        when(productService.update(any())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            productController.updateProduct(
                    "productName", "productTitle", 1, 100.0, 150.0, (byte) 1,
                    propertyAddJson, propertyUpdateJson, new Integer[]{1},
                    new String[]{"image1.jpg"}, new String[]{"detail1.jpg"}, 1);
        });
    }

    //测试正常情况下，getProductBySearch 方法
    @Test
    public void testGetProductBySearch_Success() throws UnsupportedEncodingException {
        String productName = "TestProduct";
        Integer categoryId = 1;
        Double productSalePrice = 100.0;
        Double productPrice = 150.0;
        Byte[] productIsEnabledArray = {1};
        String orderBy = "product_price";
        Boolean isDesc = true;
        Integer index = 0;
        Integer count = 10;

        Product product = new Product()
                .setProduct_name(productName)
                .setProduct_category(new Category().setCategory_id(categoryId))
                .setProduct_price(productPrice)
                .setProduct_sale_price(productSalePrice);

        OrderUtil orderUtil = new OrderUtil(orderBy, isDesc);
        PageUtil pageUtil = new PageUtil(index, count);

        List<Product> productList = Arrays.asList(new Product(), new Product());
        Integer productCount = 2;
        pageUtil.setTotal(2);

        when(productService.getList(any(Product.class),
                any(Byte[].class),
                any(OrderUtil.class),
                any(PageUtil.class))).thenReturn(productList);
        when(productService.getTotal(any(Product.class), any(Byte[].class))).thenReturn(productCount);

        // 执行
        String response = productController.getProductBySearch(productName, categoryId, productSalePrice, productPrice, productIsEnabledArray, orderBy, isDesc, index, count);

        // 断言
        JSONObject jsonObject = JSON.parseObject(response);
        assertEquals(2, jsonObject.getJSONArray("productList").size());
        assertEquals(productCount, jsonObject.getInteger("productCount"));
        assertEquals(pageUtil.getTotalPage(), jsonObject.getInteger("totalPage"));
    }

    //测试异常情况下，getProductBySearch 方法
    @Test
    public void testGetProductBySearch_Failure() {
        String productName = "TestProduct";
        Integer categoryId = 1;
        Double productSalePrice = 100.0;
        Double productPrice = 150.0;
        Byte[] productIsEnabledArray = {1};
        String orderBy = "product_price";
        Boolean isDesc = true;
        Integer index = 0;
        Integer count = 10;

        when(productService.getList(any(Product.class),
                any(Byte[].class),
                any(OrderUtil.class),
                any(PageUtil.class)))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            productController.getProductBySearch(productName, categoryId, productSalePrice, productPrice, productIsEnabledArray, orderBy, isDesc, index, count);
        });
    }

    //测试正常情况下，getPropertyByCategoryId 方法
    @Test
    public void testGetPropertyByCategoryId_Success() {
        Integer categoryId = 1;
        List<Property> propertyList = Arrays.asList(new Property(), new Property());

        when(propertyService.getList(any(Property.class), eq(null))).thenReturn(propertyList);
        String response = productController.getPropertyByCategoryId(categoryId);

        JSONObject jsonObject = JSON.parseObject(response);
        assertEquals(2, jsonObject.getJSONArray("propertyList").size());
    }

    //测试异常情况下，getPropertyByCategoryId 方法
    @Test
    public void testGetPropertyByCategoryId_Failure() {
        Integer categoryId = 1;

        when(propertyService.getList(any(Property.class), eq(null)))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            productController.getPropertyByCategoryId(categoryId);
        });
    }

    //测试正常情况下，deleteProductImageById 方法
    @Test
    public void testDeleteProductImageById_Success() {
        Integer productImageId = 1;
        ProductImage productImage = new ProductImage();

        when(productImageService.get(eq(productImageId))).thenReturn(productImage);
        when(productImageService.deleteList(any(Integer[].class))).thenReturn(true);

        String response = productController.deleteProductImageById(productImageId);

        JSONObject jsonObject = JSON.parseObject(response);
        assertTrue(jsonObject.getBoolean("success"));
    }

    //测试异常情况下，deleteProductImageById 方法
    @Test
    public void testDeleteProductImageById_Failure() {
        Integer productImageId = 1;
        ProductImage productImage = new ProductImage();

        when(productImageService.get(eq(productImageId))).thenReturn(productImage);
        when(productImageService.deleteList(any(Integer[].class))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            productController.deleteProductImageById(productImageId);
        });
    }

    //测试正常情况下，uploadProductImage 方法
    @Test
    public void testUploadProductImage_Success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        doAnswer(invocation -> {
            File destFile = invocation.getArgument(0);
            return null;
        }).when(file).transferTo(any(File.class));

        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath("/")).thenReturn("/tmp/");

        ProductController productController = new ProductController();

        String imageType = "single";
        String response = productController.uploadProductImage(file, imageType, session);

        JSONObject jsonObject = JSONObject.parseObject(response);
        assertTrue(jsonObject.getBoolean("success"));
        assertNotNull(jsonObject.getString("fileName"));
    }

    //测试异常情况下，uploadProductImage 方法
    @Test
    public void testUploadProductImage_Failure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        doThrow(new IOException("File transfer failed")).when(file).transferTo(any(File.class));

        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath("/")).thenReturn("/tmp/");

        ProductController productController = new ProductController();

        String imageType = "single";
        String response = productController.uploadProductImage(file, imageType, session);

        JSONObject jsonObject = JSONObject.parseObject(response);
        assertFalse(jsonObject.getBoolean("success")); // 期望失败
    }
}
