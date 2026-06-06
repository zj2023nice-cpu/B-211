package com.grade.system.controller;

import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.DashboardStatsDTO;
import com.grade.system.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
