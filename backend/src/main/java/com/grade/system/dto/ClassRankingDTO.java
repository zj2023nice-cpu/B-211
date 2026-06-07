package com.grade.system.dto;

import lombok.Data;

@Data
public class ClassRankingDTO {
    private Long studentId;
    private String studentName;
    private String className;
    private Double totalScore;
    private Double averageScore;
    private Integer courseCount;
    private Integer rank;
    private Boolean isTied;
}
