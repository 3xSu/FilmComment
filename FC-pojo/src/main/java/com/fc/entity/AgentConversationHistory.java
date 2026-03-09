package com.fc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI Agent对话历史实体类
 * 对应数据库表：agent_conversation_history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConversationHistory implements Serializable {

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
     * 用户ID
     */
    private Long userId;

    /**
     * 消息内容
     */
    private String messageText;

    /**
     * 消息角色：user-用户，assistant-助手
     */
    private String messageRole;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}