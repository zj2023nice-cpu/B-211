package com.grade.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class ClassProfileDTO {
    private String className;
    private Integer studentCount;
    private Integer courseCount;
    private Double averageScore;
    private Double passRate;
    private Double excellentRate;
    private List<CourseAverageDTO> courseAverages;

    @Data
    public static class CourseAverageDTO {
        private Long courseId;
        private String courseName;
        private Double averageScore;
        private Double maxScore;
        private Double minScore;
        private Integer studentCount;
        private Integer passCount;
        private Integer excellentCount;
    }
}
