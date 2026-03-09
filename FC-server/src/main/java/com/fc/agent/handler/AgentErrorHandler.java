package com.fc.agent.handler;

import com.fc.dto.agent.AgentResponse;
import com.fc.exception.AgentModelTimeoutException;
import com.fc.exception.AgentToolExecutionException;
import com.fc.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * AI Agent特定错误处理器
 * 处理Agent特有的错误逻辑，补充全局错误处理器
 */
@Slf4j
@RestControllerAdvice
public class AgentErrorHandler {
    
    /**
     * 处理Agent特定的业务逻辑错误
     * 提供更详细的Agent错误信息
     */
    @ExceptionHandler(AgentModelTimeoutException.class)
    public Result<String> handleAgentTimeout(AgentModelTimeoutException e) {
        log.warn("Agent模型超时: {}", e.getMessage());
        
        // 提供更详细的Agent错误信息
        AgentResponse response = new AgentResponse();
        response.setStatus("timeout");
        response.setResponse("抱歉，AI助手响应超时，请稍后重试");
        
        return Result.error("AI助手响应超时，请稍后重试");
    }
    
    /**
     * 处理Agent工具执行异常
     * 提供工具级别的错误信息
     */
    @ExceptionHandler(AgentToolExecutionException.class)
    public Result<String> handleAgentToolError(AgentToolExecutionException e) {
        log.error("Agent工具执行异常: {}", e.getMessage(), e);
        
        // 提供工具级别的详细错误信息
        AgentResponse response = new AgentResponse();
        response.setStatus("tool_error");
        response.setResponse("抱歉，AI助手在执行工具时遇到问题");
        
        return Result.error("AI助手工具执行失败，请稍后重试");
    }
}