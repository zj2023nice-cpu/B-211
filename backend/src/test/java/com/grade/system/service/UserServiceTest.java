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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    @DisplayName("测试更新用户 - 成功")
    void testUpdateUser_Success() {
        User updatedUser = new User();
        updatedUser.setName("张三更新");
        updatedUser.setContact("13800138000");
        updatedUser.setPassword("newpassword");
        updatedUser.setClassName("计算机2班");
        updatedUser.setRole("TEACHER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.encode("newpassword")).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testStudent);

        User result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        assertEquals("张三更新", result.getName());
        assertEquals("13800138000", result.getContact());
        assertEquals("计算机2班", result.getClassName());
        assertEquals("TEACHER", result.getRole());
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newpassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("测试更新用户 - 未找到")
    void testUpdateUser_NotFound() {
        User updatedUser = new User();
        updatedUser.setName("张三更新");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, updatedUser);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
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

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUser(1L);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }
}
