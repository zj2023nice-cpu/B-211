package com.grade.system.exception;

import com.grade.system.enums.ErrorCode;

public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND.getCode(), message);
    }
    
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super(ErrorCode.NOT_FOUND.getCode(), resourceName + " not found with id: " + resourceId);
    }
}
