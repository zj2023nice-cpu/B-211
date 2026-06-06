package com.grade.system.dto;

import lombok.Data;

@Data
public class GradeWarningDTO {
    private String studentName;
    private String className;
    private String courseName;
    private Double score;
    private Double makeupScore;
    private String term;
    private String warningLevel;
}
