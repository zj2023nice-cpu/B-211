package com.grade.system.controller;

import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.AuditLog;
import com.grade.system.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<?> getAuditLogs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        if (page != null && size != null) {
            PageResponse<AuditLog> auditLogPage = auditLogService.getAuditLogsPage(
                    username, module, action, status, startDate, endDate, page, size);
            return ApiResponse.success(auditLogPage);
        } else {
            return ApiResponse.success("请使用分页参数查询");
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<AuditLog> getAuditLog(@PathVariable Long id) {
        return ApiResponse.success("审计日志查询功能仅支持列表查询");
    }
}
