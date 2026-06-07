package com.grade.system.controller;

import com.grade.system.annotation.AuditLog;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.LoginRequest;
import com.grade.system.dto.LoginResponse;
import com.grade.system.dto.LoginUserInfo;
import com.grade.system.interceptor.AuthInterceptor;
import com.grade.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @AuditLog(module = "认证", action = "登录", description = "用户登录系统")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        logger.info("Login attempt for user: {}", request.getUsername());
        LoginResponse response = userService.login(request);
        if (response != null) {
            LoginUserInfo userInfo = new LoginUserInfo();
            userInfo.setId(response.getId());
            userInfo.setUsername(response.getUsername());
            userInfo.setRole(response.getRole());
            userInfo.setName(response.getName());
            userInfo.setClassName(response.getClassName());
            
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(AuthInterceptor.SESSION_USER_KEY, userInfo);
            
            return ApiResponse.success("登录成功", response);
        }
        logger.warn("Login failed for user: {}", request.getUsername());
        return ApiResponse.error("用户名或密码错误");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.success("退出成功");
    }
}
