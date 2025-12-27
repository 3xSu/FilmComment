package com.fc.exception;

/**
 * 权限异常
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(){

    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
