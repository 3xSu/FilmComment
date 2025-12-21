package com.fc.exception;

/**
 * 评分已存在异常
 */
public class RatingAlreadyExistsException extends BaseException {

    public RatingAlreadyExistsException() {
    }

    public RatingAlreadyExistsException(String msg) {
        super(msg);
    }
}