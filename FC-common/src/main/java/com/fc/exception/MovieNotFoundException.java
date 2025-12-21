package com.fc.exception;

public class MovieNotFoundException extends BaseException {
    public MovieNotFoundException() {
    }

    public MovieNotFoundException(String msg) {
        super(msg);
    }
}
