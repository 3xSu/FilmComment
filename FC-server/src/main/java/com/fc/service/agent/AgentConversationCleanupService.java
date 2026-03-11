package com.fc.service.agent;

import com.fc.dto.agent.CleanupResult;
import com.fc.dto.agent.CleanupStats;
import com.fc.enums.CleanupStrategyEnum;

/**
 * AI Agent对话清理服务接口
 * 
 * 提供多种清理策略，用于管理AI Agent对话历史数据的存储空间
 */
public interface AgentConversationCleanupService {
    
    /**
     * 执行自动清理任务
     * 根据配置的默认策略执行清理
     * 
     * @return 清理结果
     */
    CleanupResult executeAutoCleanup();
    
    /**
     * 基于时间的清理
     * 清理指定天数之前的对话记录
     * 
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    CleanupResult cleanupByTime(int retentionDays);
    
    /**
     * 基于数量的清理
     * 保留最近N条记录，清理超出数量的旧记录
     * 
     * @param retentionCount 保留记录数量
     * @return 清理结果
     */
    CleanupResult cleanupByCount(int retentionCount);
    
    /**
     * 基于活跃度的清理
     * 清理非活跃用户的对话记录
     * 
     * @param inactiveDays 非活跃天数阈值
     * @return 清理结果
     */
    CleanupResult cleanupByActivity(int inactiveDays);
    
    /**
     * 手动触发清理
     * 根据指定的策略和参数执行清理
     * 
     * @param strategy 清理策略
     * @param param 清理参数
     * @return 清理结果
     */
    CleanupResult manualCleanup(CleanupStrategyEnum strategy, Object param);
    
    /**
     * 获取清理统计信息
     * 
     * @return 清理统计信息
     */
    CleanupStats getCleanupStats();
    
    /**
     * 获取待清理数据统计
     * 
     * @return 待清理数据统计信息
     */
    CleanupStats getPendingCleanupStats();
    
    /**
     * 验证清理配置
     * 
     * @param strategy 清理策略
     * @param param 清理参数
     * @return 验证结果，true表示参数有效
     */
    boolean validateCleanupConfig(CleanupStrategyEnum strategy, Object param);
}