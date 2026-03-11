package com.fc.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Agent对话清理结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanupResult {
    
    /**
     * 清理的记录数量
     */
    private int affectedRows;
    
    /**
     * 清理策略
     */
    private String strategy;
    
    /**
     * 清理开始时间
     */
    private long startTime;
    
    /**
     * 清理结束时间
     */
    private long endTime;
    
    /**
     * 清理耗时（毫秒）
     */
    private long duration;
    
    /**
     * 清理是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果清理失败）
     */
    private String errorMessage;
    
    public CleanupResult(int affectedRows, String strategy) {
        this.affectedRows = affectedRows;
        this.strategy = strategy;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime;
        this.duration = 0;
        this.success = true;
    }
    
    public CleanupResult(int affectedRows, String strategy, boolean success, String errorMessage) {
        this.affectedRows = affectedRows;
        this.strategy = strategy;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime;
        this.duration = 0;
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    /**
     * 完成清理操作，计算耗时
     */
    public void complete() {
        this.endTime = System.currentTimeMillis();
        this.duration = this.endTime - this.startTime;
    }
}