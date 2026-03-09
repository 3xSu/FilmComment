package com.fc.service.agent;

import com.fc.entity.AgentConversationHistory;

import java.util.List;

/**
 * AI Agent对话历史服务接口
 */
public interface AgentConversationService {

    /**
     * 保存对话消息
     * @param conversationHistory 对话历史记录
     */
    void saveConversation(AgentConversationHistory conversationHistory);

    /**
     * 根据会话ID获取对话历史
     * @param sessionId 会话ID
     * @return 对话历史列表
     */
    List<AgentConversationHistory> getConversationHistory(String sessionId);

    /**
     * 根据用户ID获取对话历史
     * @param userId 用户ID
     * @return 对话历史列表
     */
    List<AgentConversationHistory> getConversationHistoryByUserId(Long userId);

    /**
     * 根据会话ID和用户ID获取对话历史
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 对话历史列表
     */
    List<AgentConversationHistory> getConversationHistory(String sessionId, Long userId);

    /**
     * 删除指定会话的对话历史
     * @param sessionId 会话ID
     */
    void deleteConversationHistory(String sessionId);

    /**
     * 删除指定用户的对话历史
     * @param userId 用户ID
     */
    void deleteConversationHistoryByUserId(Long userId);

    /**
     * 统计用户对话消息数量
     * @param userId 用户ID
     * @return 消息数量
     */
    int countConversationMessages(Long userId);

    /**
     * 获取用户最近一次对话时间
     * @param userId 用户ID
     * @return 最近对话时间
     */
    String getLastConversationTime(Long userId);
}