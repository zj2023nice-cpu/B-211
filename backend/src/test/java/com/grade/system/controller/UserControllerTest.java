package com.grade.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grade.system.config.TestSecurityConfig;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.User;
import com.grade.system.service.UserService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testStudent;
    private User testTeacher;

    @BeforeEach
    void setUp() {
        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setUsername("student1");
        testStudent.setPassword("123456");
        testStudent.setRole("STUDENT");
        testStudent.setName("张三");
        testStudent.setClassName("计算机1班");

        testTeacher = new User();
        testTeacher.setId(2L);
        testTeacher.setUsername("teacher1");
        testTeacher.setPassword("$2a$10$hashedPassword");
        testTeacher.setRole("TEACHER");
        testTeacher.setName("李老师");
    }

    @Test
    @DisplayName("测试获取所有用户 - 不分页")
    void testGetAllUsers_NoPagination() throws Exception {
        List<User> users = Arrays.asList(testStudent, testTeacher);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("测试获取所有用户 - 分页")
    void testGetAllUsers_WithPagination() throws Exception {
        PageResponse<User> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testStudent, testTeacher));
        pageResponse.setPageNumber(0);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);

        when(userService.getUsersPage(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(userService, times(1)).getUsersPage(0, 10);
    }

    @Test
    @DisplayName("测试创建用户")
    void testCreateUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(testStudent);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户创建成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("student1"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("测试更新用户")
    void testUpdateUser() throws Exception {
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(testStudent);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户更新成功"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    @Test
    @DisplayName("测试删除用户")
    void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户删除成功"));

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("测试获取单个用户")
    void testGetUser() throws Exception {
        when(userService.getUser(1L)).thenReturn(testStudent);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("student1"))
                .andExpect(jsonPath("$.data.name").value("张三"));

        verify(userService, times(1)).getUser(1L);
    }
}
