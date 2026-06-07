package com.grade.system.dto;

import lombok.Data;

@Data
public class LoginUserInfo {
    private Long id;
    private String username;
    private String role;
    private String name;
    private String className;
}
