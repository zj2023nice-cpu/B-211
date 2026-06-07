package com.grade.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardStatsDTO {
    private String lastLoginTime;
    private List<CourseStatDTO> courseStats;
    private Integer totalStudents;
    private Integer totalCourses;
    private Integer totalGrades;
    private Double overallAverage;
    private Integer pendingCount;
    private Integer failCourseCount;
    private Integer ungradedCount;
    private String currentPeriod;

    @Data
    public static class CourseStatDTO {
        private Long courseId;
        private String courseName;
        private Double averageScore;
        private Integer studentCount;
        private Integer passCount;
        private Integer failCount;
        private Double maxScore;
        private Double minScore;
    }
}
