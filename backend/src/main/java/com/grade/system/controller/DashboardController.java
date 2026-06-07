package com.grade.system.controller;

import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.ClassProfileDTO;
import com.grade.system.dto.DashboardStatsDTO;
import com.grade.system.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsDTO> getDashboardStats(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(required = false) String className) {

        DashboardStatsDTO stats = dashboardService.getDashboardStats(userId, username, role, className);
        return ApiResponse.success("获取统计数据成功", stats);
    }

    @GetMapping("/classes")
    public ApiResponse<List<String>> getAllClasses() {
        List<String> classes = dashboardService.getAllClassNames();
        return ApiResponse.success("获取班级列表成功", classes);
    }

    @GetMapping("/class-profile")
    public ApiResponse<ClassProfileDTO> getClassProfile(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Class", required = false) String userClass,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String term) {

        if (!"ADMIN".equals(role) && !"HEAD_TEACHER".equals(role)) {
            return ApiResponse.error("无权限访问");
        }

        String targetClass;
        if ("HEAD_TEACHER".equals(role)) {
            targetClass = userClass;
        } else {
            targetClass = className;
        }

        if (targetClass == null || targetClass.isEmpty()) {
            return ApiResponse.error("请选择班级");
        }

        ClassProfileDTO profile = dashboardService.getClassProfile(targetClass, term);
        return ApiResponse.success("获取班级画像成功", profile);
    }
}
