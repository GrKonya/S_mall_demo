package com.xq.schoolmall.controller.fore;

import com.alibaba.fastjson.JSONObject;
import com.xq.schoolmall.entity.Address;
import com.xq.schoolmall.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ForeAddressControllerTest {

    @InjectMocks
    private ForeAddressController foreAddressController;

    @Mock
    private AddressService addressService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(foreAddressController).build();
    }

    //测试正常情况下，getAddressByAreaId方法
    @Test
    void testGetAddressByAreaId_Success() throws Exception {
        Address address = new Address();
        address.setAddress_areaId("area1");

        List<Address> addressList = Arrays.asList(address);
        List<Address> childAddressList = Arrays.asList(new Address());

        when(addressService.getList(any(), anyString())).thenReturn(addressList).thenReturn(childAddressList);

        String result = mockMvc.perform(get("/address/area1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject expectedObject = new JSONObject();
        expectedObject.put("success", true);
        expectedObject.put("addressList", addressList);
        expectedObject.put("childAddressList", childAddressList);

        assertEquals(expectedObject.toJSONString(), result);
    }

    //测试未查询到地址情况下，getAddressByAreaId方法
    @Test
    void testGetAddressByAreaId_NotFound() throws Exception {
        when(addressService.getList(any(), anyString())).thenReturn(Collections.emptyList());

        String result = mockMvc.perform(get("/address/area1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject expectedObject = new JSONObject();
        expectedObject.put("success", false);

        assertEquals(expectedObject.toJSONString(), result);
    }
}