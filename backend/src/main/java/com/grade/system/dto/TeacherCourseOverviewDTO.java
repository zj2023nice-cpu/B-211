package com.grade.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class TeacherCourseOverviewDTO {
    private Long teacherId;
    private String teacherName;
    private Integer courseCount;
    private List<CourseProgressDTO> courseProgressList;

    @Data
    public static class CourseProgressDTO {
        private Long courseId;
        private String courseName;
        private Integer totalStudents;
        private Integer enteredCount;
        private Integer unscoredCount;
        private Double progressPercent;
        private String progressDescription;
        private Double averageScore;
        private Integer failCount;
        private String lastGradeChangeTime;
        private String lastGradeChangeDescription;
    }
}
