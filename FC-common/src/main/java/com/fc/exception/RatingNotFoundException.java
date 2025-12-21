package com.fc.exception;

/**
 * 评分不存在异常
 */
public class RatingNotFoundException extends BaseException {

    public RatingNotFoundException() {
    }

    public RatingNotFoundException(String msg) {
        super(msg);
    }
}