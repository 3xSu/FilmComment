package com.fc.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Agent对话清理统计信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanupStats {
    
    /**
     * 总记录数
     */
    private long totalRecords;
    
    /**
     * 已过期记录数（根据当前配置）
     */
    private long expiredRecords;
    
    /**
     * 最近一次清理时间
     */
    private long lastCleanupTime;
    
    /**
     * 最近一次清理记录数
     */
    private int lastCleanupCount;
    
    /**
     * 最近一次清理策略
     */
    private String lastCleanupStrategy;
    
    /**
     * 最近一次清理耗时（毫秒）
     */
    private long lastCleanupDuration;
    
    /**
     * 清理任务执行次数
     */
    private int cleanupExecutionCount;
    
    /**
     * 清理成功次数
     */
    private int cleanupSuccessCount;
    
    /**
     * 清理失败次数
     */
    private int cleanupFailureCount;
    
    /**
     * 平均清理耗时（毫秒）
     */
    private long averageCleanupDuration;
}