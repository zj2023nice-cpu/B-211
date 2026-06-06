package com.grade.system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ADMIN, TEACHER, HEAD_TEACHER, STUDENT

    private String name;
    private String contact;
    
    @Column(name = "class_name")
    private String className; // For students and head teachers
}
