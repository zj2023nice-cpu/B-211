package com.grade.system.service;

import com.grade.system.dto.PageResponse;
import com.grade.system.entity.AuditLog;
import com.grade.system.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PageResponse<AuditLog> getAuditLogsPage(
            String username,
            String module,
            String action,
            Boolean status,
            String startDate,
            String endDate,
            int page,
            int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            startTime = LocalDate.parse(startDate, DATE_FORMATTER).atStartOfDay();
        }
        if (endDate != null && !endDate.isEmpty()) {
            endTime = LocalDate.parse(endDate, DATE_FORMATTER).atTime(LocalTime.MAX);
        }
        
        Page<AuditLog> auditLogPage = auditLogRepository.findByConditions(
                username, module, action, status, startTime, endTime, pageable);
        
        PageResponse<AuditLog> response = new PageResponse<>();
        response.setContent(auditLogPage.getContent());
        response.setPageNumber(auditLogPage.getNumber());
        response.setPageSize(auditLogPage.getSize());
        response.setTotalElements(auditLogPage.getTotalElements());
        response.setTotalPages(auditLogPage.getTotalPages());
        response.setFirst(auditLogPage.isFirst());
        response.setLast(auditLogPage.isLast());
        return response;
    }

    public void saveAuditLog(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }

    public void createAuditLogFromJoinPoint(
            JoinPoint joinPoint,
            Object result,
            Throwable exception,
            Long userId,
            String username,
            String userRole) {
        
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            com.grade.system.annotation.AuditLog auditLogAnnotation = method.getAnnotation(com.grade.system.annotation.AuditLog.class);
            
            if (auditLogAnnotation == null) {
                return;
            }
            
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setUserRole(userRole);
            auditLog.setModule(auditLogAnnotation.module());
            auditLog.setAction(auditLogAnnotation.action());
            auditLog.setDescription(auditLogAnnotation.description());
            
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = method.getName();
            auditLog.setTargetType(className);
            
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setRequestMethod(request.getMethod());
                auditLog.setRequestPath(request.getRequestURI());
                auditLog.setIpAddress(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                
                if (auditLogAnnotation.saveParams()) {
                    Map<String, String[]> parameterMap = request.getParameterMap();
                    if (!parameterMap.isEmpty()) {
                        try {
                            auditLog.setRequestParams(objectMapper.writeValueAsString(parameterMap));
                        } catch (JsonProcessingException e) {
                            auditLog.setRequestParams(parameterMap.toString());
                        }
                    } else {
                        Object[] args = joinPoint.getArgs();
                        if (args != null && args.length > 0) {
                            try {
                                Map<String, Object> argsMap = new HashMap<>();
                                String[] paramNames = signature.getParameterNames();
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] != null && !isSensitiveType(args[i])) {
                                        String paramName = paramNames != null && paramNames.length > i ? paramNames[i] : "arg" + i;
                                        argsMap.put(paramName, args[i]);
                                    }
                                }
                                if (!argsMap.isEmpty()) {
                                    auditLog.setRequestParams(objectMapper.writeValueAsString(argsMap));
                                }
                            } catch (Exception e) {
                                auditLog.setRequestParams("参数解析失败");
                            }
                        }
                    }
                }
            }
            
            if (exception != null) {
                auditLog.setStatus(false);
                String errorMsg = exception.getMessage();
                if (errorMsg != null && errorMsg.length() > 1000) {
                    errorMsg = errorMsg.substring(0, 1000);
                }
                auditLog.setErrorMessage(errorMsg);
            } else {
                auditLog.setStatus(true);
                if (auditLogAnnotation.saveResult() && result != null) {
                    try {
                        String resultStr = objectMapper.writeValueAsString(result);
                        if (resultStr.length() > 2000) {
                            resultStr = resultStr.substring(0, 2000);
                        }
                        auditLog.setResponseResult(resultStr);
                    } catch (JsonProcessingException e) {
                        auditLog.setResponseResult("结果解析失败");
                    }
                }
            }
            
            saveAuditLog(auditLog);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean isSensitiveType(Object obj) {
        if (obj == null) return true;
        String className = obj.getClass().getSimpleName().toLowerCase();
        return className.contains("password") || 
               className.contains("token") || 
               className.contains("secret") ||
               className.contains("credential");
    }
}
