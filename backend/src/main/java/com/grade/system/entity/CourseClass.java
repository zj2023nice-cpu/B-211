package com.grade.system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course_classes",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_course_class", 
                             columnNames = {"course_id", "class_name"})
       })
@Data
public class CourseClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "class_name", nullable = false)
    private String className;
}
