package com.fc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI Agent工具调用记录实体类
 * 对应数据库表：agent_tool_usage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentToolUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具参数（JSON格式）
     */
    private String toolParameters;

    /**
     * 执行时间（毫秒）
     */
    private Integer executionTimeMs;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}