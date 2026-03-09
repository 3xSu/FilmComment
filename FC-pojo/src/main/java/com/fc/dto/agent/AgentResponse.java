package com.fc.dto.agent;

import lombok.Data;

/**
 * AI Agent响应结果
 * 
 * @author AI Assistant
 * @since 2026-03-08
 */
@Data
public class AgentResponse {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * Agent响应内容
     */
    private String response;
    
    /**
     * 响应状态：success/error
     */
    private String status;
}