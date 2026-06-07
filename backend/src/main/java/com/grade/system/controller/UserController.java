package com.grade.system.controller;

import com.grade.system.annotation.AuditLog;
import com.grade.system.context.UserContext;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.dto.ResetPasswordRequest;
import com.grade.system.dto.UserImportResult;
import com.grade.system.entity.User;
import com.grade.system.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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

    @AuditLog(module = "用户管理", action = "重置密码", description = "重置用户密码", saveParams = false)
    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) ResetPasswordRequest request) {
        if (!UserContext.isLoggedIn()) {
            return ApiResponse.error("用户未登录");
        }
        if (!"ADMIN".equals(UserContext.getUserRole())) {
            return ApiResponse.error("无权限重置用户密码");
        }
        
        String newPassword = request != null ? request.getNewPassword() : null;
        userService.resetPassword(id, newPassword);
        return ApiResponse.success("密码重置成功", null);
    }

    @AuditLog(module = "用户管理", action = "导入", description = "批量导入用户")
    @PostMapping("/import")
    public ApiResponse<UserImportResult> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            return ApiResponse.error("只支持CSV格式的文件");
        }

        try {
            UserImportResult result = userService.importUsersFromCsv(file);
            if (result.getFailCount() > 0) {
                return ApiResponse.success("导入完成，成功 " + result.getSuccessCount() + " 条，失败 " + result.getFailCount() + " 条", result);
            }
            return ApiResponse.success("成功导入 " + result.getSuccessCount() + " 条用户记录", result);
        } catch (Exception e) {
            return ApiResponse.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=user_import_template.csv");

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
        writer.println('\uFEFF' + "用户名,姓名,角色,班级,联系方式");
        writer.println("zhangsan,张三,STUDENT,计算机2023-1班,13800138000");
        writer.println("lisi,李四,TEACHER,,lisi@school.edu");
        writer.println("wangwu,王五,HEAD_TEACHER,计算机2023-2班,13900139000");
        writer.flush();
        writer.close();
    }
}
