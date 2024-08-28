package com.xq.schoolmall.controller.fore;
import com.alibaba.fastjson.JSONObject;
import com.xq.schoolmall.entity.Address;
import com.xq.schoolmall.entity.User;
import com.xq.schoolmall.service.AddressService;
import com.xq.schoolmall.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class ForeUserControllerTest {

    @InjectMocks
    private ForeUserController foreUserController;

    @Mock
    private AddressService addressService;

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    @Mock
    private ServletContext servletContext;

    @Mock
    private Map<String, Object> map;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(session.getServletContext()).thenReturn(servletContext);
    }

    @Test
    public void testGoToUserDetail_Success() {
        // 准备测试数据
        Integer userId = 1;
        Address address = new Address().setAddress_areaId("110101").setAddress_regionId(new Address().setAddress_areaId("110100"));
        User user = new User().setUser_id(userId).setUser_address(address);

        List<Address> addressList = new ArrayList<>();
        List<Address> cityList = new ArrayList<>();
        List<Address> districtList = new ArrayList<>();

        // 模拟session和服务层
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.get(userId)).thenReturn(user);
        when(addressService.get("110101")).thenReturn(address);
        when(addressService.get("110100")).thenReturn(address);
        when(addressService.getRoot()).thenReturn(addressList);
        when(addressService.getList(null, "110100")).thenReturn(cityList);
        when(addressService.getList(null, "110101")).thenReturn(districtList);

        // 调用方法
        String result = foreUserController.goToUserDetail(session, mock(Map.class));

        // 验证结果
        assertEquals("fore/userDetails", result);
        verify(userService, times(1)).get(userId);
        verify(addressService, times(1)).get("110101");
    }


    @Test
    public void testGoToUserDetail_UserNotLoggedIn() {
        // 模拟session未登录
        when(session.getAttribute("userId")).thenReturn(null);

        // 调用方法
        String result = foreUserController.goToUserDetail(session, mock(Map.class));

        // 验证结果
        assertEquals("redirect:/login", result);
        verify(userService, never()).get(anyInt());
    }

    @Test
    public void testUploadUserHeadImage_Success() throws Exception {
        // 模拟文件上传
        MockMultipartFile file = new MockMultipartFile("file", "filename.jpg", "image/jpeg", "some image".getBytes());
        String tempDir = System.getProperty("java.io.tmpdir");
        String uploadPath = tempDir + "res/images/item/userProfilePicture/";

        // 确保目录存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        when(servletContext.getRealPath("/")).thenReturn(tempDir);

        // 调用方法
        String result = foreUserController.uploadUserHeadImage(file, session);

        // 解析结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        assertEquals(true, jsonObject.getBoolean("success"));
        verify(session, times(1)).getServletContext();
    }

    @Test
    public void testUploadUserHeadImage_Failure() throws Exception {
        // 模拟文件上传时抛出异常
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("filename.jpg");
        when(servletContext.getRealPath("/")).thenReturn(System.getProperty("java.io.tmpdir"));
        doThrow(new IOException("模拟文件上传失败")).when(file).transferTo(any(File.class));

        // 调用方法
        String result = foreUserController.uploadUserHeadImage(file, session);

        // 解析结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        assertEquals(false, jsonObject.getBoolean("success"));
        verify(session, times(1)).getServletContext();
    }

    @Test
    public void testUserUpdate_Success() throws Exception {
        // 准备测试数据
        Integer userId = 1;
        String nickname = "new_nickname";
        String realname = "new_realname";
        String gender = "1";
        String birthday = "1990-01-01";
        String address = "110101";
        String profilePicture = "new_pic.jpg";
        String password = "new_password";

        User user = new User().setUser_id(userId);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.get(userId)).thenReturn(user);
        when(userService.update(any(User.class))).thenReturn(true);

        // 调用方法
        String result = foreUserController.userUpdate(session, map, nickname, realname, gender, birthday, address, profilePicture, password);

        // 验证结果
        assertEquals("redirect:/userDetails", result);
        verify(userService, times(1)).update(any(User.class));
        verify(map, times(1)).put("user", user);
    }

    @Test(expected = RuntimeException.class)
    public void testUserUpdate_Failure() throws Exception {
        // 准备测试数据
        Integer userId = 1;
        String nickname = "new_nickname";
        String realname = "new_realname";
        String gender = "1";
        String birthday = "1990-01-01";
        String address = "110101";
        String profilePicture = "new_pic.jpg";
        String password = "new_password";

        User user = new User().setUser_id(userId);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.get(userId)).thenReturn(user);
        when(userService.update(any(User.class))).thenReturn(false);  // 模拟更新失败

        // 调用方法
        foreUserController.userUpdate(session, map, nickname, realname, gender, birthday, address, profilePicture, password);
    }
}
