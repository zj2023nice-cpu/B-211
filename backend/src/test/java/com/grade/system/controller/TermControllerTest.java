package com.grade.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grade.system.config.TestSecurityConfig;
import com.grade.system.context.UserContext;
import com.grade.system.dto.LoginUserInfo;
import com.grade.system.entity.Term;
import com.grade.system.service.TermService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TermController.class)
@Import(TestSecurityConfig.class)
class TermControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TermService termService;

    @Autowired
    private ObjectMapper objectMapper;

    private Term testTerm1;
    private Term testTerm2;

    @BeforeEach
    void setUp() {
        testTerm1 = new Term();
        testTerm1.setId(1L);
        testTerm1.setName("2023-2024-1");
        testTerm1.setEnabled(true);
        testTerm1.setSortOrder(10);

        testTerm2 = new Term();
        testTerm2.setId(2L);
        testTerm2.setName("2022-2023-2");
        testTerm2.setEnabled(false);
        testTerm2.setSortOrder(5);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("测试获取学期列表 - 未登录")
    void testGetTerms_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户未登录"));

        verify(termService, never()).getAllTerms();
    }

    @Test
    @DisplayName("测试获取学期列表 - 非管理员")
    void testGetTerms_NonAdmin() throws Exception {
        UserContext.setUser(createLoginUser("TEACHER"));

        mockMvc.perform(get("/api/terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无权限访问学期管理"));

        verify(termService, never()).getAllTerms();
    }

    @Test
    @DisplayName("测试获取学期列表 - 管理员")
    void testGetTerms_Admin() throws Exception {
        UserContext.setUser(createLoginUser("ADMIN"));
        List<Term> terms = Arrays.asList(testTerm1, testTerm2);
        when(termService.getAllTerms()).thenReturn(terms);

        mockMvc.perform(get("/api/terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(termService, times(1)).getAllTerms();
    }

    @Test
    @DisplayName("测试新增学期 - 非管理员")
    void testCreateTerm_NonAdmin() throws Exception {
        UserContext.setUser(createLoginUser("TEACHER"));

        mockMvc.perform(post("/api/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTerm1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限维护学期"));

        verify(termService, never()).createTerm(any(Term.class));
    }

    @Test
    @DisplayName("测试新增学期 - 管理员")
    void testCreateTerm_Admin() throws Exception {
        UserContext.setUser(createLoginUser("ADMIN"));
        when(termService.createTerm(any(Term.class))).thenReturn(testTerm1);

        mockMvc.perform(post("/api/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTerm1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("创建成功"))
                .andExpect(jsonPath("$.data.name").value("2023-2024-1"));

        verify(termService, times(1)).createTerm(any(Term.class));
    }

    @Test
    @DisplayName("测试获取全部学期名称 - 兼容开放读取")
    void testGetAllTermNames_OpenAccess() throws Exception {
        when(termService.getAllTermNames()).thenReturn(Arrays.asList("2023-2024-1", "2021-2022-1"));

        mockMvc.perform(get("/api/terms/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0]").value("2023-2024-1"));

        verify(termService, times(1)).getAllTermNames();
    }

    private LoginUserInfo createLoginUser(String role) {
        LoginUserInfo user = new LoginUserInfo();
        user.setId(1L);
        user.setUsername("tester");
        user.setRole(role);
        user.setName("测试用户");
        return user;
    }
}
