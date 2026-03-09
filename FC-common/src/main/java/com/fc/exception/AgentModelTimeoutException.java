package com.fc.exception;

/**
 * AI Agent模型响应超时异常
 */
public class AgentModelTimeoutException extends BaseException {
    public AgentModelTimeoutException() {

    }

    public AgentModelTimeoutException(String message) {
        super(message);
    }
}