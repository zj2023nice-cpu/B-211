package com.grade.system.enums;

public enum ErrorCode {
    
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    
    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_ALREADY_EXISTS(2002, "课程已存在"),
    
    GRADE_NOT_FOUND(3001, "成绩记录不存在"),
    GRADE_ALREADY_EXISTS(3002, "该学生在该学期已存在此课程的成绩记录"),
    
    DATA_INTEGRITY_VIOLATION(4001, "数据完整性约束违反"),
    FILE_READ_ERROR(4002, "文件读取失败"),
    FILE_FORMAT_ERROR(4003, "文件格式错误");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
