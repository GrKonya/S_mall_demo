package com.xq.schoolmall.controller.fore;

import com.alibaba.fastjson.JSONObject;
import com.xq.schoolmall.entity.User;
import com.xq.schoolmall.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ForeLoginControllerTest {

    @InjectMocks
    private ForeLoginController foreLoginController;

    @Mock
    private UserService userService;

    private MockHttpSession session;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(foreLoginController).build();
        session = new MockHttpSession();
    }

    //测试转到登录页，goToPage方法
    @Test
    void testGoToPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("fore/loginPage"));
    }

    //测试登陆成功时，checkLogin方法
    @Test
    void testCheckLogin_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUser_id(1);
        when(userService.login("validUser", "validPassword")).thenReturn(mockUser);

        mockMvc.perform(post("/login/doLogin")
                        .param("username", "validUser")
                        .param("password", "validPassword"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));
    }

    //测试登陆失败时，checkLogin方法
    @Test
    void testCheckLogin_Failure() throws Exception {
        when(userService.login("invalidUser", "invalidPassword")).thenReturn(null);

        mockMvc.perform(post("/login/doLogin")
                        .param("username", "invalidUser")
                        .param("password", "invalidPassword"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":false}"));
    }

    //测试登出成功时，logout方法
    @Test
    void testLogout_Success() throws Exception {
        session.setAttribute("userId", 1);

        // Act: Perform the logout request
        mockMvc.perform(get("/login/logout")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    //测试用户不存在时，返回登录页
    @Test
    void testLogout_NoUserId() throws Exception {

        mockMvc.perform(get("/login/logout")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
