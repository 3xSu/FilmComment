package com.fc.exception;

/**
 * 无效评分异常
 */
public class InvalidRatingException extends BaseException {

    public InvalidRatingException() {
    }

    public InvalidRatingException(String msg) {
        super(msg);
    }

}
