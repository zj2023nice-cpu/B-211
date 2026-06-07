package com.grade.system.controller;

import com.grade.system.context.UserContext;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.AuditLog;
import com.grade.system.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/my")
    public ApiResponse<?> getMyAuditLogs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        if (!UserContext.isLoggedIn()) {
            return ApiResponse.error("用户未登录");
        }
        
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("无效的用户身份");
        }
        
        if (page != null && size != null) {
            PageResponse<AuditLog> auditLogPage = auditLogService.getMyAuditLogsPage(
                    userId, module, action, status, startDate, endDate, page, size);
            return ApiResponse.success(auditLogPage);
        } else {
            return ApiResponse.success("请使用分页参数查询");
        }
    }

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

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        byte[] csvData = auditLogService.exportAuditLogsToCsv(
                username, module, action, status, startDate, endDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String fileName = "audit_logs_" + LocalDateTime.now().format(formatter) + ".csv";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(csvData.length);
        
        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
}
