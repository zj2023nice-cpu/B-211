package com.grade.system.exception;

import com.grade.system.enums.ErrorCode;

public class DuplicateResourceException extends BusinessException {
    
    public DuplicateResourceException(String message) {
        super(ErrorCode.DATA_INTEGRITY_VIOLATION.getCode(), message);
    }
    
    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(ErrorCode.DATA_INTEGRITY_VIOLATION.getCode(), 
              resourceName + " with " + fieldName + " '" + fieldValue + "' already exists");
    }
}
