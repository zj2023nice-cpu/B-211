package com.grade.system.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String name;
    private String contact;
    private String role;
    private String className;
}
