package com.grade.system.controller;

import com.grade.system.annotation.AuditLog;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.User;
import com.grade.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<?> getAllUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PageResponse<User> userPage = userService.getUsersPage(page, size);
            return ApiResponse.success(userPage);
        } else {
            List<User> users = userService.getAllUsers();
            return ApiResponse.success(users);
        }
    }
    
    @AuditLog(module = "用户管理", action = "新增", description = "新增用户")
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ApiResponse.success("用户创建成功", createdUser);
    }
    
    @AuditLog(module = "用户管理", action = "修改", description = "修改用户信息")
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ApiResponse.success("用户更新成功", updatedUser);
    }
    
    @AuditLog(module = "用户管理", action = "删除", description = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("用户删除成功", null);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return ApiResponse.success(user);
    }
}
