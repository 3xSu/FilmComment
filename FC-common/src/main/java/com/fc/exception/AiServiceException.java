package com.fc.exception;

public class AiServiceException extends BaseException {
    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message);
    }
}