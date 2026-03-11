package com.fc.agent.config;

import com.fc.enums.CleanupStrategyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI Agent对话清理配置类
 * 
 * 用于配置AI Agent历史对话的自动清理策略和参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "agent.conversation.cleanup")
public class AgentCleanupConfig {
    
    /**
     * 是否启用自动清理功能
     */
    private boolean enabled = true;
    
    /**
     * 清理策略
     * Spring Boot会自动将字符串转换为枚举
     */
    private CleanupStrategyEnum strategy = CleanupStrategyEnum.TIME_BASED;
    
    /**
     * 数据保留天数（仅对TIME_BASED策略有效）
     */
    private int retentionDays = 30;
    
    /**
     * 保留记录数量（仅对COUNT_BASED策略有效）
     */
    private int retentionCount = 1000;
    
    /**
     * 批量删除数量
     */
    private int batchSize = 1000;
    
    /**
     * 执行时间（格式：HH:mm）
     */
    private String executionTime = "04:00";
    
    /**
     * 是否启用统计功能
     */
    private boolean enableStatistics = true;
    
    /**
     * 是否记录详细日志
     */
    private boolean logDetails = true;
    
    /**
     * 验证配置参数的有效性
     * 
     * @return 如果配置有效返回true，否则返回false
     */
    public boolean isValid() {
        if (retentionDays <= 0) {
            return false;
        }
        if (retentionCount <= 0) {
            return false;
        }
        if (batchSize <= 0) {
            return false;
        }
        if (executionTime == null || !executionTime.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            return false;
        }
        return true;
    }
    
    /**
     * 获取默认配置实例
     * 
     * @return 默认配置实例
     */
    public static AgentCleanupConfig getDefault() {
        AgentCleanupConfig config = new AgentCleanupConfig();
        config.setEnabled(true);
        config.setStrategy(CleanupStrategyEnum.TIME_BASED);
        config.setRetentionDays(30);
        config.setRetentionCount(1000);
        config.setBatchSize(1000);
        config.setExecutionTime("04:00");
        config.setEnableStatistics(true);
        config.setLogDetails(true);
        return config;
    }
}