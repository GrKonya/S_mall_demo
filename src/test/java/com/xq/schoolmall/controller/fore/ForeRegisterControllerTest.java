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
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ForeRegisterControllerTest {

    @InjectMocks
    private ForeRegisterController foreRegisterController;

    @Mock
    private AddressService addressService;

    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGoToPage_Success() {
        // 准备测试数据
        ModelMap map = new ModelMap();
        List<Address> addressList = new ArrayList<>();
        List<Address> cityAddress = new ArrayList<>();
        List<Address> districtAddress = new ArrayList<>();

        when(addressService.getRoot()).thenReturn(addressList);
        when(addressService.getList(null, "110000")).thenReturn(cityAddress);
        when(addressService.getList(null, "110100")).thenReturn(districtAddress);

        // 调用方法
        String result = foreRegisterController.goToPage(map);

        // 验证结果
        assertEquals("fore/register", result);
        verify(addressService, times(1)).getRoot();
        verify(addressService, times(1)).getList(null, "110000");
        verify(addressService, times(1)).getList(null, "110100");

        assertEquals(addressList, map.get("addressList"));
        assertEquals(cityAddress, map.get("cityList"));
        assertEquals(districtAddress, map.get("districtList"));
    }

    @Test(expected = RuntimeException.class)
    public void testGoToPage_Failure() {
        // 准备测试数据
        ModelMap map = new ModelMap();

        when(addressService.getRoot()).thenThrow(new RuntimeException());

        // 调用方法
        foreRegisterController.goToPage(map);

        // 验证结果
        verify(addressService, times(1)).getRoot();
    }

    @Test
    public void testRegister_Success() throws Exception {
        // 准备测试数据
        String userName = "testUser";
        String userNickname = "testNickname";
        String userPassword = "testPassword";
        String userGender = "1";
        String userBirthday = "2000-01-01";
        String userAddress = "110100";

        when(userService.getTotal(any())).thenReturn(0);
        when(userService.add(any())).thenReturn(true);

        // 调用方法
        String result = foreRegisterController.register(
                userName,
                userNickname,
                userPassword,
                userGender,
                userBirthday,
                userAddress
        );

        // 验证结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        assertEquals(true, jsonObject.getBoolean("success"));

        verify(userService, times(1)).getTotal(any());
        verify(userService, times(1)).add(any(User.class));
    }

    @Test
    public void testRegister_UsernameExists() throws Exception {
        // 准备测试数据
        String userName = "testUser";
        String userNickname = "testNickname";
        String userPassword = "testPassword";
        String userGender = "1";
        String userBirthday = "2000-01-01";
        String userAddress = "110100";

        when(userService.getTotal(any())).thenReturn(1);

        // 调用方法
        String result = foreRegisterController.register(
                userName,
                userNickname,
                userPassword,
                userGender,
                userBirthday,
                userAddress
        );

        // 验证结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        assertEquals(false, jsonObject.getBoolean("success"));
        assertEquals("用户名已存在，请重新输入！", jsonObject.getString("msg"));

        verify(userService, times(1)).getTotal(any());
        verify(userService, times(0)).add(any(User.class));
    }

    @Test(expected = RuntimeException.class)
    public void testRegister_AddUserFailure() throws Exception {
        // 准备测试数据
        String userName = "testUser";
        String userNickname = "testNickname";
        String userPassword = "testPassword";
        String userGender = "1";
        String userBirthday = "2000-01-01";
        String userAddress = "110100";

        when(userService.getTotal(any())).thenReturn(0);
        when(userService.add(any())).thenReturn(false);

        // 调用方法
        foreRegisterController.register(
                userName,
                userNickname,
                userPassword,
                userGender,
                userBirthday,
                userAddress
        );

        // 验证结果
        verify(userService, times(1)).getTotal(any());
        verify(userService, times(1)).add(any(User.class));
    }
}
