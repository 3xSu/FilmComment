package com.fc.exception;

/**
 * 无权限异常
 */
public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message);
    }
}