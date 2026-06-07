package com.grade.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grade.system.config.TestSecurityConfig;
import com.grade.system.context.UserContext;
import com.grade.system.dto.ChangePasswordRequest;
import com.grade.system.dto.LoginUserInfo;
import com.grade.system.dto.PageResponse;
import com.grade.system.dto.ResetPasswordRequest;
import com.grade.system.dto.UserProfileUpdateRequest;
import com.grade.system.entity.User;
import com.grade.system.service.UserService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        testStudent.setContact("13800138000");

        testTeacher = new User();
        testTeacher.setId(2L);
        testTeacher.setUsername("teacher1");
        testTeacher.setPassword("$2a$10$hashedPassword");
        testTeacher.setRole("TEACHER");
        testTeacher.setName("李老师");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("测试获取所有用户 - 未登录")
    void testGetAllUsers_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户未登录"));

        verify(userService, never()).getAllUsers();
    }

    @Test
    @DisplayName("测试获取所有用户 - 非管理员")
    void testGetAllUsers_NonAdmin() throws Exception {
        UserContext.setUser(createLoginUser(1L, "TEACHER"));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限查看用户列表"));

        verify(userService, never()).getAllUsers();
    }

    @Test
    @DisplayName("测试获取所有用户 - 管理员不分页")
    void testGetAllUsers_NoPagination() throws Exception {
        UserContext.setUser(createLoginUser(99L, "ADMIN"));
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
    @DisplayName("测试获取所有用户 - 管理员分页")
    void testGetAllUsers_WithPagination() throws Exception {
        UserContext.setUser(createLoginUser(99L, "ADMIN"));
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
    @DisplayName("测试创建用户 - 非管理员")
    void testCreateUser_NonAdmin() throws Exception {
        UserContext.setUser(createLoginUser(1L, "TEACHER"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限新增用户"));

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("测试创建用户 - 管理员")
    void testCreateUser_Admin() throws Exception {
        UserContext.setUser(createLoginUser(99L, "ADMIN"));
        when(userService.createUser(any(User.class))).thenReturn(testStudent);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("用户创建成功"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("测试本人更新资料")
    void testUpdateUser_SelfProfile() throws Exception {
        UserContext.setUser(createLoginUser(1L, "STUDENT"));
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setName("张三更新");
        request.setContact("13900139000");

        when(userService.updateUserProfile(eq(1L), eq("张三更新"), eq("13900139000"))).thenReturn(testStudent);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("用户更新成功"));

        verify(userService, times(1)).updateUserProfile(1L, "张三更新", "13900139000");
        verify(userService, never()).updateUserByAdmin(anyLong(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试本人无权修改角色或班级")
    void testUpdateUser_SelfCannotChangeRoleOrClass() throws Exception {
        UserContext.setUser(createLoginUser(1L, "STUDENT"));
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setRole("TEACHER");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限修改角色或班级"));

        verify(userService, never()).updateUserProfile(anyLong(), any(), any());
    }

    @Test
    @DisplayName("测试管理员更新用户资料")
    void testUpdateUser_Admin() throws Exception {
        UserContext.setUser(createLoginUser(99L, "ADMIN"));
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setName("张三更新");
        request.setContact("13900139000");
        request.setRole("HEAD_TEACHER");
        request.setClassName("计算机2班");

        when(userService.updateUserByAdmin(eq(1L), eq("张三更新"), eq("13900139000"), eq("HEAD_TEACHER"), eq("计算机2班")))
                .thenReturn(testStudent);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService, times(1)).updateUserByAdmin(1L, "张三更新", "13900139000", "HEAD_TEACHER", "计算机2班");
    }

    @Test
    @DisplayName("测试普通用户不能修改他人资料")
    void testUpdateUser_ForbiddenForOtherUser() throws Exception {
        UserContext.setUser(createLoginUser(1L, "STUDENT"));
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setName("李四");

        mockMvc.perform(put("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限修改其他用户的信息"));

        verify(userService, never()).updateUserProfile(anyLong(), any(), any());
        verify(userService, never()).updateUserByAdmin(anyLong(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试删除用户 - 非管理员")
    void testDeleteUser_NonAdmin() throws Exception {
        UserContext.setUser(createLoginUser(1L, "TEACHER"));

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限删除用户"));

        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    @DisplayName("测试删除用户 - 管理员")
    void testDeleteUser_Admin() throws Exception {
        UserContext.setUser(createLoginUser(99L, "ADMIN"));
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("用户删除成功"));

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("测试获取单个用户 - 本人")
    void testGetUser_Self() throws Exception {
        UserContext.setUser(createLoginUser(1L, "STUDENT"));
        when(userService.getUser(1L)).thenReturn(testStudent);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(userService, times(1)).getUser(1L);
    }

    @Test
    @DisplayName("测试获取单个用户 - 无权查看他人")
    void testGetUser_Forbidden() throws Exception {
        UserContext.setUser(createLoginUser(1L, "STUDENT"));

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限查看其他用户的信息"));

        verify(userService, never()).getUser(anyLong());
    }

    @Test
    @DisplayName("测试管理员重置密码")
    void testResetPassword_Admin() throws Exception {
        UserContext.setUser(createLoginUser(99L, "ADMIN"));
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newpass123");

        mockMvc.perform(put("/api/users/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密码重置成功"));

        verify(userService, times(1)).resetPassword(1L, "newpass123");
    }

    @Test
    @DisplayName("测试非管理员不能重置密码")
    void testResetPassword_NonAdmin() throws Exception {
        UserContext.setUser(createLoginUser(1L, "TEACHER"));

        mockMvc.perform(put("/api/users/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限重置用户密码"));

        verify(userService, never()).resetPassword(anyLong(), any());
    }

    @Test
    @DisplayName("测试本人修改密码成功")
    void testChangePassword_SelfSuccess() throws Exception {
        UserContext.setUser(createLoginUser(1L, "STUDENT"));
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("123456");
        request.setNewPassword("newpass123");
        when(userService.changePassword(1L, "123456", "newpass123")).thenReturn(true);

        mockMvc.perform(put("/api/users/1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        verify(userService, times(1)).changePassword(1L, "123456", "newpass123");
    }

    @Test
    @DisplayName("测试不能修改他人密码")
    void testChangePassword_Forbidden() throws Exception {
        UserContext.setUser(createLoginUser(1L, "STUDENT"));
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("123456");
        request.setNewPassword("newpass123");

        mockMvc.perform(put("/api/users/2/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限修改其他用户的密码"));

        verify(userService, never()).changePassword(anyLong(), any(), any());
    }

    private LoginUserInfo createLoginUser(Long id, String role) {
        LoginUserInfo user = new LoginUserInfo();
        user.setId(id);
        user.setUsername("tester");
        user.setRole(role);
        user.setName("测试用户");
        return user;
    }
}
