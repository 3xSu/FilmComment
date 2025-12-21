package com.fc.exception;

public class MovieAlreadyExistsException extends BaseException {
    public MovieAlreadyExistsException() {
    }

    public MovieAlreadyExistsException(String msg) {
        super(msg);
    }
}
