package com.grade.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grade.system.dto.DashboardStatsDTO;
import com.grade.system.entity.AuditLog;
import com.grade.system.entity.Course;
import com.grade.system.entity.Grade;
import com.grade.system.repository.AuditLogRepository;
import com.grade.system.repository.CourseRepository;
import com.grade.system.repository.GradeRepository;
import com.grade.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private TermService termService;

    @InjectMocks
    private DashboardService dashboardService;

    @SuppressWarnings("unused")
    private ObjectMapper objectMapper = new ObjectMapper();

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("数学");

        Page<AuditLog> emptyAuditLogPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(auditLogRepository.findByConditions(eq("tester"), eq("认证"), eq("登录"), eq(true), eq(null), eq(null), any()))
                .thenReturn(emptyAuditLogPage);
        when(courseRepository.findAll()).thenReturn(Collections.singletonList(testCourse));
        when(userRepository.findByRole("STUDENT")).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("周统计应包含周一起始边界")
    void shouldIncludeWeekStartBoundary() {
        LocalDateTime weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        Grade boundaryGrade = buildGrade(1L, 80.0, "2024-Fall", weekStart);
        Grade beforeBoundaryGrade = buildGrade(2L, 90.0, "2024-Fall", weekStart.minusSeconds(1));

        when(gradeRepository.findAll()).thenReturn(Arrays.asList(boundaryGrade, beforeBoundaryGrade));

        DashboardStatsDTO stats = dashboardService.getDashboardStats(null, "tester", "ADMIN", null, "week");

        assertNotNull(stats);
        assertEquals(1, stats.getTotalGrades());
        assertEquals(1, stats.getCourseStats().size());
        assertEquals(80.0, stats.getOverallAverage());
    }

    @Test
    @DisplayName("月统计应包含月初起始边界")
    void shouldIncludeMonthStartBoundary() {
        LocalDateTime monthStart = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();

        Grade boundaryGrade = buildGrade(1L, 75.0, "2024-Fall", monthStart);
        Grade beforeBoundaryGrade = buildGrade(2L, 88.0, "2024-Fall", monthStart.minusSeconds(1));

        when(gradeRepository.findAll()).thenReturn(Arrays.asList(boundaryGrade, beforeBoundaryGrade));

        DashboardStatsDTO stats = dashboardService.getDashboardStats(null, "tester", "ADMIN", null, "month");

        assertNotNull(stats);
        assertEquals(1, stats.getTotalGrades());
        assertEquals(1, stats.getCourseStats().size());
        assertEquals(75.0, stats.getOverallAverage());
    }

    @Test
    @DisplayName("学期统计应兼容命中有历史成绩的学期")
    void shouldUseResolvedDashboardTerm() {
        Grade historicalGrade = buildGrade(1L, 92.0, "2023-Fall", LocalDateTime.now());
        Grade otherGrade = buildGrade(2L, 66.0, "2024-Fall", LocalDateTime.now());

        when(gradeRepository.findAll()).thenReturn(Arrays.asList(historicalGrade, otherGrade));
        when(termService.resolveDashboardTermName()).thenReturn(Optional.of("2023-Fall"));

        DashboardStatsDTO stats = dashboardService.getDashboardStats(null, "tester", "ADMIN", null, "term");

        assertNotNull(stats);
        assertEquals(1, stats.getTotalGrades());
        assertEquals(1, stats.getCourseStats().size());
        assertEquals(92.0, stats.getOverallAverage());
    }

    @Test
    @DisplayName("学期解析失败时应回退为当前相关成绩而非空")
    void shouldFallbackWhenDashboardTermMissing() {
        Grade grade = buildGrade(1L, 85.0, "2023-Fall", LocalDateTime.now());

        when(gradeRepository.findAll()).thenReturn(Collections.singletonList(grade));
        when(termService.resolveDashboardTermName()).thenReturn(Optional.empty());

        DashboardStatsDTO stats = dashboardService.getDashboardStats(null, "tester", "ADMIN", null, "term");

        assertNotNull(stats);
        assertEquals(1, stats.getTotalGrades());
        assertEquals(1, stats.getCourseStats().size());
        assertEquals(85.0, stats.getOverallAverage());
    }

    private Grade buildGrade(Long studentId, Double score, String term, LocalDateTime createdAt) {
        Grade grade = new Grade();
        grade.setId(studentId);
        grade.setStudentId(studentId);
        grade.setCourseId(testCourse.getId());
        grade.setScore(score);
        grade.setTerm(term);
        grade.setCreatedAt(createdAt);
        return grade;
    }
}
