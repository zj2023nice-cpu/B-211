package com.grade.system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "grades", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_student_course_term", 
                             columnNames = {"student_id", "course_id", "term"})
       })
@Data
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private Long studentId;
    
    @Column(name = "course_id")
    private Long courseId;
    
    private Double score;
    
    @Column(name = "makeup_score")
    private Double makeupScore;
    
    private String term;
}
