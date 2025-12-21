package com.fc.exception;

/**
 * 深度讨论区权限不足异常
 */
public class SpoilerPermissionDeniedException extends BaseException {

    public SpoilerPermissionDeniedException() {
        super("深度讨论区权限不足");
    }

    public SpoilerPermissionDeniedException(String message) {
        super(message);
    }

    public SpoilerPermissionDeniedException(String message, Throwable cause) {
        super(message);
    }
}