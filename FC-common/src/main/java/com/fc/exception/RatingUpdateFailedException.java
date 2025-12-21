package com.fc.exception;

public class RatingUpdateFailedException extends BaseException {
    public RatingUpdateFailedException(String message) {
        super(message);
    }

    public RatingUpdateFailedException(String message, Throwable cause) {
        super(message);
    }
}
