package com.normcat.common.exception;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(ErrorCode.DUPLICATE_RESOURCE,
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
