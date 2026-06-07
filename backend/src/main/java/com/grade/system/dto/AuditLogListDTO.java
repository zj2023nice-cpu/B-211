package com.grade.system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogListDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userRole;
    private String action;
    private String module;
    private String description;
    private String targetType;
    private String targetId;
    private String requestMethod;
    private String requestPath;
    private String ipAddress;
    private Boolean status;
    private LocalDateTime createdAt;
}
