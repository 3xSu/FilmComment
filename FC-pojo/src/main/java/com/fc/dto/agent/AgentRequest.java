package com.fc.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI Agent请求参数
 * 
 * 用于与电影推荐Agent进行会话对话，支持上下文管理
 * 用户ID从JWT token中自动获取，无需手动传入
 */
@Data
@Schema(description = "AI Agent会话请求参数")
public class AgentRequest {
    
    /**
     * 会话ID，用于管理对话上下文
     * 为空时系统自动生成UUID
     */
    @Schema(description = "会话ID，为空时自动生成", example = "123e4567-e89b-12d3-a456-426614174000")
    private String sessionId;
    
    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容", example = "推荐一些科幻电影")
    private String message;
    
    /**
     * 是否重置上下文
     */
    @Schema(description = "是否重置对话上下文", example = "false")
    private Boolean resetContext = false;
}