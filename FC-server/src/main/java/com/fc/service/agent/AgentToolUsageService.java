package com.fc.service.agent;

import com.fc.entity.AgentToolUsage;

import java.util.List;

/**
 * AI Agent工具调用记录服务接口
 */
public interface AgentToolUsageService {

    /**
     * 记录工具调用
     * @param toolUsage 工具调用记录
     */
    void recordToolUsage(AgentToolUsage toolUsage);

    /**
     * 根据会话ID获取工具调用记录
     * @param sessionId 会话ID
     * @return 工具调用记录列表
     */
    List<AgentToolUsage> getToolUsageBySessionId(String sessionId);

    /**
     * 根据工具名称获取工具调用记录
     * @param toolName 工具名称
     * @return 工具调用记录列表
     */
    List<AgentToolUsage> getToolUsageByToolName(String toolName);

    /**
     * 统计工具调用次数
     * @param toolName 工具名称
     * @return 调用次数
     */
    int countToolUsage(String toolName);

    /**
     * 获取工具调用成功率
     * @param toolName 工具名称
     * @return 成功率（0-1）
     */
    double getToolSuccessRate(String toolName);

    /**
     * 获取工具平均执行时间
     * @param toolName 工具名称
     * @return 平均执行时间（毫秒）
     */
    int getAverageExecutionTime(String toolName);

    /**
     * 删除指定会话的工具调用记录
     * @param sessionId 会话ID
     */
    void deleteToolUsageBySessionId(String sessionId);

    /**
     * 清理指定时间之前的工具调用记录
     * @param daysBefore 天数
     */
    void cleanupOldToolUsage(int daysBefore);

    /**
     * 获取工具使用统计信息
     * @return 统计信息
     */
    String getToolUsageStatistics();
}