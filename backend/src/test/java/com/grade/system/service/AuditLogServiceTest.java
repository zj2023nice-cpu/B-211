package com.grade.system.service;

import com.grade.system.dto.PageResponse;
import com.grade.system.entity.AuditLog;
import com.grade.system.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog log1;
    private AuditLog log2;
    private AuditLog log3;
    private AuditLog log4;

    @BeforeEach
    void setUp() {
        log1 = new AuditLog();
        log1.setId(1L);
        log1.setUserId(1L);
        log1.setUsername("admin");
        log1.setUserRole("ADMIN");
        log1.setModule("用户管理");
        log1.setAction("新增");
        log1.setStatus(true);
        log1.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        log1.setRequestParams("{\"username\":\"test\"}");
        log1.setResponseResult("{\"success\":true}");
        log1.setErrorMessage(null);
        log1.setUserAgent("Mozilla/5.0");

        log2 = new AuditLog();
        log2.setId(2L);
        log2.setUserId(2L);
        log2.setUsername("teacher1");
        log2.setUserRole("TEACHER");
        log2.setModule("成绩管理");
        log2.setAction("修改");
        log2.setStatus(true);
        log2.setCreatedAt(LocalDateTime.of(2024, 1, 16, 14, 20));
        log2.setRequestParams("{\"score\":85}");
        log2.setResponseResult("{\"success\":true}");
        log2.setErrorMessage(null);
        log2.setUserAgent("Mozilla/5.0");

        log3 = new AuditLog();
        log3.setId(3L);
        log3.setUserId(1L);
        log3.setUsername("admin");
        log3.setUserRole("ADMIN");
        log3.setModule("用户管理");
        log3.setAction("删除");
        log3.setStatus(false);
        log3.setCreatedAt(LocalDateTime.of(2024, 1, 17, 9, 15));
        log3.setRequestParams("{\"id\":123}");
        log3.setResponseResult(null);
        log3.setErrorMessage("用户不存在");
        log3.setUserAgent("Chrome/120");

        log4 = new AuditLog();
        log4.setId(4L);
        log4.setUserId(3L);
        log4.setUsername("student1");
        log4.setUserRole("STUDENT");
        log4.setModule("成绩查询");
        log4.setAction("查询");
        log4.setStatus(true);
        log4.setCreatedAt(LocalDateTime.of(2024, 1, 18, 16, 45));
        log4.setRequestParams("{\"studentId\":3}");
        log4.setResponseResult("{\"data\":[]}");
        log4.setErrorMessage(null);
        log4.setUserAgent("Safari/17");
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 无条件查询")
    void testGetAuditLogsPage_NoConditions() {
        List<AuditLog> logs = Arrays.asList(log1, log2, log3, log4);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, null, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(4, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(4, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        verify(auditLogRepository, times(1)).findByConditions(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 按用户名模糊查询")
    void testGetAuditLogsPage_ByUsername() {
        List<AuditLog> logs = Arrays.asList(log1, log3);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                eq("admin"), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                "admin", null, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(log -> "admin".equals(log.getUsername())));
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 按模块查询")
    void testGetAuditLogsPage_ByModule() {
        List<AuditLog> logs = Arrays.asList(log2);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                isNull(), eq("成绩管理"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, "成绩管理", null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("成绩管理", result.getContent().get(0).getModule());
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 按操作类型查询")
    void testGetAuditLogsPage_ByAction() {
        List<AuditLog> logs = Arrays.asList(log1, log2);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                isNull(), isNull(), eq("新增"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, null, "新增", null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(log -> "新增".equals(log.getAction())));
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 按状态查询（成功）")
    void testGetAuditLogsPage_ByStatusSuccess() {
        List<AuditLog> logs = Arrays.asList(log1, log2, log4);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                isNull(), isNull(), isNull(), eq(true), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, null, null, true, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(log -> Boolean.TRUE.equals(log.getStatus())));
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 按状态查询（失败）")
    void testGetAuditLogsPage_ByStatusFailure() {
        List<AuditLog> logs = Arrays.asList(log3);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                isNull(), isNull(), isNull(), eq(false), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, null, null, false, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.getContent().get(0).getStatus());
        assertEquals("用户不存在", result.getContent().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 按日期范围查询")
    void testGetAuditLogsPage_ByDateRange() {
        List<AuditLog> logs = Arrays.asList(log1, log2, log3);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                isNull(), isNull(), isNull(), isNull(),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, null, null, null, "2024-01-01", "2024-01-17", 0, 10);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 多条件组合查询")
    void testGetAuditLogsPage_MultipleConditions() {
        List<AuditLog> logs = Arrays.asList(log1);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                eq("admin"), eq("用户管理"), eq("新增"), eq(true),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                "admin", "用户管理", "新增", true, "2024-01-01", "2024-01-31", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        AuditLog resultLog = result.getContent().get(0);
        assertEquals("admin", resultLog.getUsername());
        assertEquals("用户管理", resultLog.getModule());
        assertEquals("新增", resultLog.getAction());
        assertTrue(resultLog.getStatus());
    }

    @Test
    @DisplayName("测试分页查询审计日志 - 返回结果已清除敏感字段")
    void testGetAuditLogsPage_SensitiveFieldsCleared() {
        List<AuditLog> logs = Arrays.asList(log1, log3);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByConditions(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, null, null, null, null, null, 0, 10);

        assertNotNull(result);
        for (AuditLog log : result.getContent()) {
            assertNull(log.getRequestParams());
            assertNull(log.getResponseResult());
            assertNull(log.getErrorMessage());
            assertNull(log.getUserAgent());
        }
    }

    @Test
    @DisplayName("测试分页查询我的审计日志 - 按用户ID和条件查询")
    void testGetMyAuditLogsPage_ByUserIdAndConditions() {
        List<AuditLog> logs = Arrays.asList(log1, log3);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByUserIdAndConditions(
                eq(1L), eq("用户管理"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getMyAuditLogsPage(
                1L, "用户管理", null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(auditLogRepository, times(1)).findByUserIdAndConditions(
                eq(1L), eq("用户管理"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("测试分页查询我的审计日志 - 敏感字段已清除")
    void testGetMyAuditLogsPage_SensitiveFieldsCleared() {
        List<AuditLog> logs = Arrays.asList(log1);
        Page<AuditLog> logPage = new PageImpl<>(logs);

        when(auditLogRepository.findByUserIdAndConditions(
                eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getMyAuditLogsPage(
                1L, null, null, null, null, null, 0, 10);

        assertNotNull(result);
        AuditLog resultLog = result.getContent().get(0);
        assertNull(resultLog.getRequestParams());
        assertNull(resultLog.getResponseResult());
        assertNull(resultLog.getErrorMessage());
        assertNull(resultLog.getUserAgent());
    }

    @Test
    @DisplayName("测试分页查询 - 第一页")
    void testGetAuditLogsPage_FirstPage() {
        List<AuditLog> logs = Arrays.asList(log1, log2);
        Page<AuditLog> logPage = new PageImpl<>(logs, org.springframework.data.domain.PageRequest.of(0, 2), 4);

        when(auditLogRepository.findByConditions(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, null, null, null, null, null, 0, 2);

        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(2, result.getPageSize());
        assertEquals(4, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
    }

    @Test
    @DisplayName("测试分页查询 - 最后一页")
    void testGetAuditLogsPage_LastPage() {
        List<AuditLog> logs = Arrays.asList(log3, log4);
        Page<AuditLog> logPage = new PageImpl<>(logs, org.springframework.data.domain.PageRequest.of(1, 2), 4);

        when(auditLogRepository.findByConditions(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(logPage);

        PageResponse<AuditLog> result = auditLogService.getAuditLogsPage(
                null, null, null, null, null, null, 1, 2);

        assertNotNull(result);
        assertEquals(1, result.getPageNumber());
        assertEquals(2, result.getPageSize());
        assertEquals(4, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
    }

    @Test
    @DisplayName("测试导出CSV - 按条件导出")
    void testExportAuditLogsToCsv_WithConditions() {
        List<AuditLog> logs = Arrays.asList(log1, log2);

        when(auditLogRepository.findByConditionsWithoutPage(
                eq("admin"), isNull(), isNull(), isNull(),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(logs);

        byte[] result = auditLogService.exportAuditLogsToCsv(
                "admin", null, null, null, "2024-01-01", "2024-01-31");

        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("操作时间,用户名,角色,模块,操作,请求方法,请求路径,状态,错误信息"));
        assertTrue(csvContent.contains("admin"));
        assertTrue(csvContent.contains("管理员"));
        verify(auditLogRepository, times(1)).findByConditionsWithoutPage(
                eq("admin"), isNull(), isNull(), isNull(),
                any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("测试导出CSV - 状态字段正确显示")
    void testExportAuditLogsToCsv_StatusDisplay() {
        List<AuditLog> logs = Arrays.asList(log1, log3);

        when(auditLogRepository.findByConditionsWithoutPage(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(logs);

        byte[] result = auditLogService.exportAuditLogsToCsv(
                null, null, null, null, null, null);

        assertNotNull(result);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("成功"));
        assertTrue(csvContent.contains("失败"));
        assertTrue(csvContent.contains("用户不存在"));
    }

    @Test
    @DisplayName("测试根据ID获取审计日志详情")
    void testGetAuditLogById() {
        when(auditLogRepository.findById(1L)).thenReturn(java.util.Optional.of(log1));

        AuditLog result = auditLogService.getAuditLogById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin", result.getUsername());
        assertNotNull(result.getRequestParams());
        assertNotNull(result.getResponseResult());
        verify(auditLogRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("测试根据ID和用户ID获取审计日志详情")
    void testGetAuditLogByIdAndUserId() {
        when(auditLogRepository.findByIdAndUserId(1L, 1L)).thenReturn(java.util.Optional.of(log1));

        AuditLog result = auditLogService.getAuditLogByIdAndUserId(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        verify(auditLogRepository, times(1)).findByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("测试保存审计日志")
    void testSaveAuditLog() {
        AuditLog newLog = new AuditLog();
        newLog.setUsername("test");
        newLog.setModule("测试模块");
        newLog.setAction("测试操作");

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(newLog);

        auditLogService.saveAuditLog(newLog);

        verify(auditLogRepository, times(1)).save(newLog);
    }
}
