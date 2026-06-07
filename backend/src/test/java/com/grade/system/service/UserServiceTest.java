package com.grade.system.service;

import com.grade.system.dto.LoginRequest;
import com.grade.system.dto.LoginResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.User;
import com.grade.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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

    @Test
    @DisplayName("测试登录 - 明文密码成功")
    void testLogin_PlainPassword_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testStudent);

        LoginResponse result = userService.login(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("student1", result.getUsername());
        assertEquals("STUDENT", result.getRole());
        assertEquals("张三", result.getName());
        assertEquals("计算机1班", result.getClassName());
        verify(userRepository, times(1)).findByUsername("student1");
        verify(passwordEncoder, times(1)).encode("123456");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("测试登录 - BCrypt密码成功")
    void testLogin_BCryptPassword_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("teacher1");
        request.setPassword("123456");

        when(userRepository.findByUsername("teacher1")).thenReturn(Optional.of(testTeacher));
        when(passwordEncoder.matches("123456", "$2a$10$hashedPassword")).thenReturn(true);

        LoginResponse result = userService.login(request);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("teacher1", result.getUsername());
        assertEquals("TEACHER", result.getRole());
        verify(userRepository, times(1)).findByUsername("teacher1");
        verify(passwordEncoder, times(1)).matches("123456", "$2a$10$hashedPassword");
    }

    @Test
    @DisplayName("测试登录 - 用户名不存在")
    void testLogin_UsernameNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("123456");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        LoginResponse result = userService.login(request);

        assertNull(result);
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("测试登录 - 密码错误")
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(testStudent));

        LoginResponse result = userService.login(request);

        assertNull(result);
        verify(userRepository, times(1)).findByUsername("student1");
    }

    @Test
    @DisplayName("测试获取所有用户")
    void testGetAllUsers() {
        List<User> users = Arrays.asList(testStudent, testTeacher);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试分页获取用户")
    void testGetUsersPage() {
        List<User> users = Arrays.asList(testStudent, testTeacher);
        Page<User> userPage = new PageImpl<>(users);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<User> result = userService.getUsersPage(0, 10);

        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getTotalPages());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("测试创建用户 - 带密码")
    void testCreateUser_WithPassword() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("mypassword");
        newUser.setRole("STUDENT");
        newUser.setName("新用户");

        when(passwordEncoder.encode("mypassword")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.createUser(newUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(passwordEncoder, times(1)).encode("mypassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("测试创建用户 - 不带密码（使用默认密码）")
    void testCreateUser_WithoutPassword() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword(null);
        newUser.setRole("STUDENT");
        newUser.setName("新用户");

        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.createUser(newUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(passwordEncoder, times(1)).encode("123456");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("测试本人更新资料 - 只修改姓名和联系方式")
    void testUpdateUserProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(userRepository.save(any(User.class))).thenReturn(testStudent);

        User result = userService.updateUserProfile(1L, "张三更新", "13900139000");

        assertNotNull(result);
        assertEquals("张三更新", result.getName());
        assertEquals("13900139000", result.getContact());
        assertEquals("STUDENT", result.getRole());
        assertEquals("计算机1班", result.getClassName());
        assertEquals("123456", result.getPassword());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("测试管理员更新用户资料")
    void testUpdateUserByAdmin_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(userRepository.save(any(User.class))).thenReturn(testStudent);

        User result = userService.updateUserByAdmin(1L, "张三更新", "13900139000", "HEAD_TEACHER", "计算机2班");

        assertNotNull(result);
        assertEquals("张三更新", result.getName());
        assertEquals("13900139000", result.getContact());
        assertEquals("HEAD_TEACHER", result.getRole());
        assertEquals("计算机2班", result.getClassName());
        assertEquals("123456", result.getPassword());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("测试更新用户资料 - 未找到")
    void testUpdateUserProfile_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updateUserProfile(1L, "张三更新", null));

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试重置密码 - 自定义新密码")
    void testResetPassword_WithNewPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$newEncodedPassword");

        userService.resetPassword(1L, "newpass123");

        assertEquals("$2a$10$newEncodedPassword", testStudent.getPassword());
        verify(passwordEncoder, times(1)).encode("newpass123");
        verify(userRepository, times(1)).save(testStudent);
    }

    @Test
    @DisplayName("测试重置密码 - 默认密码")
    void testResetPassword_DefaultPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$defaultEncodedPassword");

        userService.resetPassword(1L, null);

        assertEquals("$2a$10$defaultEncodedPassword", testStudent.getPassword());
        verify(passwordEncoder, times(1)).encode("123456");
        verify(userRepository, times(1)).save(testStudent);
    }

    @Test
    @DisplayName("测试修改密码 - 旧密码正确")
    void testChangePassword_Success() {
        testTeacher.setPassword("$2a$10$hashedPassword");
        when(userRepository.findById(2L)).thenReturn(Optional.of(testTeacher));
        when(passwordEncoder.matches("oldpass", "$2a$10$hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$newEncodedPassword");

        boolean result = userService.changePassword(2L, "oldpass", "newpass123");

        assertTrue(result);
        assertEquals("$2a$10$newEncodedPassword", testTeacher.getPassword());
        verify(passwordEncoder, times(1)).matches("oldpass", "$2a$10$hashedPassword");
        verify(passwordEncoder, times(1)).encode("newpass123");
        verify(userRepository, times(1)).save(testTeacher);
    }

    @Test
    @DisplayName("测试修改密码 - 旧密码错误")
    void testChangePassword_WrongOldPassword() {
        testTeacher.setPassword("$2a$10$hashedPassword");
        when(userRepository.findById(2L)).thenReturn(Optional.of(testTeacher));
        when(passwordEncoder.matches("wrongpass", "$2a$10$hashedPassword")).thenReturn(false);

        boolean result = userService.changePassword(2L, "wrongpass", "newpass123");

        assertFalse(result);
        verify(passwordEncoder, times(1)).matches("wrongpass", "$2a$10$hashedPassword");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试删除用户")
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("测试获取用户 - 成功")
    void testGetUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        User result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("student1", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("测试获取用户 - 未找到")
    void testGetUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUser(1L));

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("测试登录 - $2b$前缀BCrypt密码成功")
    void testLogin_2bBCryptPassword_Success() {
        User userWith2bHash = new User();
        userWith2bHash.setId(3L);
        userWith2bHash.setUsername("user2b");
        userWith2bHash.setPassword("$2b$10$hashedPassword");
        userWith2bHash.setRole("ADMIN");
        userWith2bHash.setName("2b用户");

        LoginRequest request = new LoginRequest();
        request.setUsername("user2b");
        request.setPassword("123456");

        when(userRepository.findByUsername("user2b")).thenReturn(Optional.of(userWith2bHash));
        when(passwordEncoder.matches("123456", "$2b$10$hashedPassword")).thenReturn(true);

        LoginResponse result = userService.login(request);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("user2b", result.getUsername());
        assertEquals("ADMIN", result.getRole());
        verify(passwordEncoder, times(1)).matches("123456", "$2b$10$hashedPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试登录 - $2y$前缀BCrypt密码成功")
    void testLogin_2yBCryptPassword_Success() {
        User userWith2yHash = new User();
        userWith2yHash.setId(4L);
        userWith2yHash.setUsername("user2y");
        userWith2yHash.setPassword("$2y$10$hashedPassword");
        userWith2yHash.setRole("STUDENT");
        userWith2yHash.setName("2y用户");

        LoginRequest request = new LoginRequest();
        request.setUsername("user2y");
        request.setPassword("123456");

        when(userRepository.findByUsername("user2y")).thenReturn(Optional.of(userWith2yHash));
        when(passwordEncoder.matches("123456", "$2y$10$hashedPassword")).thenReturn(true);

        LoginResponse result = userService.login(request);

        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("user2y", result.getUsername());
        assertEquals("STUDENT", result.getRole());
        verify(passwordEncoder, times(1)).matches("123456", "$2y$10$hashedPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试登录 - 明文密码迁移后密码确实被更新")
    void testLogin_PlainPassword_PasswordIsUpdated() {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals("$2a$10$newHashedPassword", savedUser.getPassword());
            return savedUser;
        });

        LoginResponse result = userService.login(request);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("测试修改密码 - 旧密码为明文时验证成功并更新为BCrypt")
    void testChangePassword_OldPlainPassword_Success() {
        testStudent.setPassword("oldpass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$newEncodedPassword");

        boolean result = userService.changePassword(1L, "oldpass", "newpass123");

        assertTrue(result);
        assertEquals("$2a$10$newEncodedPassword", testStudent.getPassword());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, times(1)).encode("newpass123");
        verify(userRepository, times(1)).save(testStudent);
    }

    @Test
    @DisplayName("测试修改密码 - 旧密码为明文时验证失败")
    void testChangePassword_OldPlainPassword_WrongPassword() {
        testStudent.setPassword("oldpass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        boolean result = userService.changePassword(1L, "wrongpass", "newpass123");

        assertFalse(result);
        assertEquals("oldpass", testStudent.getPassword());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试导入CSV用户 - 成功导入")
    void testImportUsersFromCsv_Success() throws Exception {
        String csvContent = "用户名,姓名,角色,班级,联系方式\n" +
                "newuser1,新用户1,STUDENT,计算机1班,13800138001\n" +
                "newuser2,新用户2,TEACHER,,13800138002";

        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csvContent.getBytes());

        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser2")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$defaultEncoded");
        when(userRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        assertTrue(result.getErrors().isEmpty());
        verify(userRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("测试导入CSV用户 - 空文件")
    void testImportUsersFromCsv_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", new byte[0]);

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(0, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        assertFalse(result.getErrors().isEmpty());
        assertEquals("CSV文件为空", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("测试导入CSV用户 - 用户名已存在")
    void testImportUsersFromCsv_UsernameExists() throws Exception {
        String csvContent = "用户名,姓名,角色,班级,联系方式\n" +
                "student1,张三,STUDENT,计算机1班,13800138001";

        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csvContent.getBytes());

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(testStudent));

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals("用户名已存在", result.getErrors().get(0).getErrorMessage());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("测试导入CSV用户 - 文件内重复用户名")
    void testImportUsersFromCsv_DuplicateUsernameInFile() throws Exception {
        String csvContent = "用户名,姓名,角色,班级,联系方式\n" +
                "user1,用户1,STUDENT,计算机1班,13800138001\n" +
                "user1,用户2,STUDENT,计算机1班,13800138002";

        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csvContent.getBytes());

        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$defaultEncoded");
        when(userRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(2, result.getTotal());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals("文件中存在重复的用户名", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("测试导入CSV用户 - 无效角色")
    void testImportUsersFromCsv_InvalidRole() throws Exception {
        String csvContent = "用户名,姓名,角色,班级,联系方式\n" +
                "baduser,坏用户,INVALID_ROLE,计算机1班,13800138001";

        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csvContent.getBytes());

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("角色非法"));
    }

    @Test
    @DisplayName("测试导入CSV用户 - 学生角色缺少班级")
    void testImportUsersFromCsv_StudentMissingClass() throws Exception {
        String csvContent = "用户名,姓名,角色,班级,联系方式\n" +
                "studentx,学生X,STUDENT,,13800138001";

        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csvContent.getBytes());

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("班级字段不能为空"));
    }

    @Test
    @DisplayName("测试导入CSV用户 - 行数据不完整")
    void testImportUsersFromCsv_IncompleteRow() throws Exception {
        String csvContent = "用户名,姓名,角色,班级,联系方式\n" +
                "partial,只填了用户名";

        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csvContent.getBytes());

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("行数据不完整"));
    }

    @Test
    @DisplayName("测试导入CSV用户 - 空用户名")
    void testImportUsersFromCsv_EmptyUsername() throws Exception {
        String csvContent = "用户名,姓名,角色,班级,联系方式\n" +
                ",张三,STUDENT,计算机1班,13800138001";

        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csvContent.getBytes());

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(1, result.getTotal());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals("用户名不能为空", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("测试导入CSV用户 - 混合成功与失败")
    void testImportUsersFromCsv_MixedSuccessAndFailure() throws Exception {
        String csvContent = "用户名,姓名,角色,班级,联系方式\n" +
                "gooduser,好用户,STUDENT,计算机1班,13800138001\n" +
                "badrole,坏角色,INVALID,计算机1班,13800138002\n" +
                "gooduser2,好用户2,TEACHER,,13800138003";

        MockMultipartFile file = new MockMultipartFile(
                "file", "users.csv", "text/csv", csvContent.getBytes());

        when(userRepository.findByUsername("gooduser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("gooduser2")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$defaultEncoded");
        when(userRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        com.grade.system.dto.UserImportResult result = userService.importUsersFromCsv(file);

        assertEquals(3, result.getTotal());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals(1, result.getErrors().size());
    }
}
