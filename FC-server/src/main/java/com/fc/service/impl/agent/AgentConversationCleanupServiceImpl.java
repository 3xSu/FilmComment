package com.fc.service.impl.agent;

import com.fc.dto.agent.CleanupResult;
import com.fc.dto.agent.CleanupStats;
import com.fc.entity.AgentConversationHistory;
import com.fc.enums.CleanupStrategyEnum;
import com.fc.mapper.agent.AgentConversationHistoryMapper;
import com.fc.service.agent.AgentConversationCleanupService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI Agent对话清理服务实现类
 * 
 * 负责管理AI Agent对话历史数据的清理任务，支持多种清理策略
 * 包括基于时间、基于数量、基于活跃度的清理方式
 */
@Slf4j
@Service
public class AgentConversationCleanupServiceImpl implements AgentConversationCleanupService {

    @Autowired
    private AgentConversationHistoryMapper agentConversationHistoryMapper;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    // 监控指标
    private Timer agentCleanupTimer;
    private Counter agentCleanupCounter;
    private Counter agentCleanupExecutionCounter;
    private Counter agentCleanupFailureCounter;
    private Gauge agentCleanupPendingGauge;
    
    // 清理统计信息
    private final AtomicInteger cleanupExecutionCount = new AtomicInteger(0);
    private final AtomicInteger cleanupSuccessCount = new AtomicInteger(0);
    private final AtomicInteger cleanupFailureCount = new AtomicInteger(0);
    private final AtomicLong totalCleanupDuration = new AtomicLong(0);
    private volatile long lastCleanupTime = 0;
    private volatile int lastCleanupCount = 0;
    private volatile String lastCleanupStrategy = "";
    private volatile long lastCleanupDuration = 0;
    
    @PostConstruct
    public void initMonitoring() {
        // 初始化监控指标
        agentCleanupTimer = meterRegistry.timer("agent.cleanup.time");
        agentCleanupCounter = meterRegistry.counter("agent.cleanup.records");
        agentCleanupExecutionCounter = meterRegistry.counter("agent.cleanup.executions");
        agentCleanupFailureCounter = meterRegistry.counter("agent.cleanup.failures");
        
        // 初始化Gauge指标
        agentCleanupPendingGauge = Gauge.builder("agent.cleanup.pending", 
                this, AgentConversationCleanupServiceImpl::getPendingRecordsCount)
                .description("待清理的Agent对话记录数量")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public CleanupResult executeAutoCleanup() {
        // 默认使用基于时间的清理策略，保留30天数据
        return cleanupByTime(30);
    }

    @Override
    @Transactional
    public CleanupResult cleanupByTime(int retentionDays) {
        log.info("开始执行基于时间的AI Agent对话清理，保留天数: {}", retentionDays);
        
        if (retentionDays <= 0) {
            String errorMsg = "保留天数必须大于0，当前值: " + retentionDays;
            log.error(errorMsg);
            return new CleanupResult(0, CleanupStrategyEnum.TIME_BASED.name(), false, errorMsg);
        }
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        CleanupResult result = new CleanupResult(0, CleanupStrategyEnum.TIME_BASED.name());
        
        // 记录清理任务执行次数
        agentCleanupExecutionCounter.increment();
        
        try {
            // 统计待清理数据量
            int pendingCount = agentConversationHistoryMapper.countByCreateTimeBefore(cutoffTime);
            log.info("待清理的过期对话记录数量: {}", pendingCount);
            
            if (pendingCount == 0) {
                log.info("没有需要清理的过期对话记录");
                result.complete();
                updateStatistics(result, true);
                return result;
            }
            
            // 使用Timer监控清理执行时间
            int affectedRows = agentCleanupTimer.record(() -> {
                return agentConversationHistoryMapper.deleteByCreateTimeBefore(cutoffTime);
            });
            
            result.setAffectedRows(affectedRows);
            result.complete();
            
            // 记录清理记录数量
            agentCleanupCounter.increment(affectedRows);
            
            log.info("基于时间清理完成，保留天数: {}, 清理记录数: {}, 耗时: {}ms", 
                    retentionDays, affectedRows, result.getDuration());
            
            updateStatistics(result, true);
            return result;
            
        } catch (Exception e) {
            log.error("基于时间清理失败，保留天数: {}, 错误: {}", retentionDays, e.getMessage(), e);
            result.complete();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            
            // 记录清理失败次数
            agentCleanupFailureCounter.increment();
            
            updateStatistics(result, false);
            return result;
        }
    }

    @Override
    @Transactional
    public CleanupResult cleanupByCount(int retentionCount) {
        log.info("开始执行基于数量的AI Agent对话清理，保留记录数: {}", retentionCount);
        
        if (retentionCount <= 0) {
            String errorMsg = "保留记录数必须大于0，当前值: " + retentionCount;
            log.error(errorMsg);
            return new CleanupResult(0, CleanupStrategyEnum.COUNT_BASED.name(), false, errorMsg);
        }
        
        CleanupResult result = new CleanupResult(0, CleanupStrategyEnum.COUNT_BASED.name());
        
        // 记录清理任务执行次数
        agentCleanupExecutionCounter.increment();
        
        try {
            // 获取总记录数
            int totalCount = getTotalRecordCount();
            log.info("当前总对话记录数: {}", totalCount);
            
            if (totalCount <= retentionCount) {
                log.info("当前记录数 {} 小于等于保留数 {}，无需清理", totalCount, retentionCount);
                result.complete();
                updateStatistics(result, true);
                return result;
            }
            
            // 计算需要删除的记录数
            int recordsToDelete = totalCount - retentionCount;
            log.info("需要清理的记录数: {}", recordsToDelete);
            
            // 获取最早的记录时间作为截止时间
            LocalDateTime cutoffTime = getCutoffTimeForCount(retentionCount);
            if (cutoffTime == null) {
                log.warn("无法确定截止时间，跳过基于数量的清理");
                result.complete();
                updateStatistics(result, true);
                return result;
            }
            
            // 使用Timer监控清理执行时间
            int affectedRows = agentCleanupTimer.record(() -> {
                return agentConversationHistoryMapper.deleteByCreateTimeBefore(cutoffTime);
            });
            
            result.setAffectedRows(affectedRows);
            result.complete();
            
            // 记录清理记录数量
            agentCleanupCounter.increment(affectedRows);
            
            log.info("基于数量清理完成，保留记录数: {}, 清理记录数: {}, 耗时: {}ms", 
                    retentionCount, affectedRows, result.getDuration());
            
            updateStatistics(result, true);
            return result;
            
        } catch (Exception e) {
            log.error("基于数量清理失败，保留记录数: {}, 错误: {}", retentionCount, e.getMessage(), e);
            result.complete();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            
            // 记录清理失败次数
            agentCleanupFailureCounter.increment();
            
            updateStatistics(result, false);
            return result;
        }
    }

    @Override
    @Transactional
    public CleanupResult cleanupByActivity(int inactiveDays) {
        log.info("开始执行基于活跃度的AI Agent对话清理，非活跃天数: {}", inactiveDays);
        
        if (inactiveDays <= 0) {
            String errorMsg = "非活跃天数必须大于0，当前值: " + inactiveDays;
            log.error(errorMsg);
            return new CleanupResult(0, CleanupStrategyEnum.ACTIVITY_BASED.name(), false, errorMsg);
        }
        
        CleanupResult result = new CleanupResult(0, CleanupStrategyEnum.ACTIVITY_BASED.name());
        
        // 记录清理任务执行次数
        agentCleanupExecutionCounter.increment();
        
        try {
            // 获取非活跃用户列表
            List<Long> inactiveUserIds = getInactiveUserIds(inactiveDays);
            
            if (inactiveUserIds.isEmpty()) {
                log.info("没有找到非活跃用户，无需清理");
                result.complete();
                updateStatistics(result, true);
                return result;
            }
            
            log.info("找到 {} 个非活跃用户，开始清理对话记录", inactiveUserIds.size());
            
            // 使用Timer监控清理执行时间
            int totalAffectedRows = agentCleanupTimer.record(() -> {
                int affectedRows = 0;
                for (Long userId : inactiveUserIds) {
                    try {
                        affectedRows += deleteConversationHistoryByUserId(userId);
                        log.debug("清理用户 {} 的对话记录，删除记录数: {}", userId, affectedRows);
                    } catch (Exception e) {
                        log.warn("清理用户 {} 的对话记录失败: {}", userId, e.getMessage());
                    }
                }
                return affectedRows;
            });
            
            result.setAffectedRows(totalAffectedRows);
            result.complete();
            
            // 记录清理记录数量
            agentCleanupCounter.increment(totalAffectedRows);
            
            log.info("基于活跃度清理完成，非活跃天数: {}, 清理用户数: {}, 清理记录数: {}, 耗时: {}ms", 
                    inactiveDays, inactiveUserIds.size(), totalAffectedRows, result.getDuration());
            
            updateStatistics(result, true);
            return result;
            
        } catch (Exception e) {
            log.error("基于活跃度清理失败，非活跃天数: {}, 错误: {}", inactiveDays, e.getMessage(), e);
            result.complete();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            
            // 记录清理失败次数
            agentCleanupFailureCounter.increment();
            
            updateStatistics(result, false);
            return result;
        }
    }

    @Override
    @Transactional
    public CleanupResult manualCleanup(CleanupStrategyEnum strategy, Object param) {
        log.info("开始执行手动清理，策略: {}, 参数: {}", strategy, param);
        
        if (!validateCleanupConfig(strategy, param)) {
            String errorMsg = "清理配置验证失败，策略: " + strategy + ", 参数: " + param;
            log.error(errorMsg);
            return new CleanupResult(0, strategy.name(), false, errorMsg);
        }
        
        try {
            CleanupResult result;
            switch (strategy) {
                case TIME_BASED:
                    result = cleanupByTime((Integer) param);
                    break;
                case COUNT_BASED:
                    result = cleanupByCount((Integer) param);
                    break;
                case ACTIVITY_BASED:
                    result = cleanupByActivity((Integer) param);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的清理策略: " + strategy);
            }
            
            log.info("手动清理完成，策略: {}, 参数: {}, 结果: {}", strategy, param, result);
            return result;
            
        } catch (Exception e) {
            log.error("手动清理失败，策略: {}, 参数: {}, 错误: {}", strategy, param, e.getMessage(), e);
            return new CleanupResult(0, strategy.name(), false, e.getMessage());
        }
    }

    @Override
    public CleanupStats getCleanupStats() {
        CleanupStats stats = new CleanupStats();
        
        // 获取总记录数
        stats.setTotalRecords(getTotalRecordCount());
        
        // 获取过期记录数（默认保留30天）
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
        stats.setExpiredRecords(agentConversationHistoryMapper.countByCreateTimeBefore(cutoffTime));
        
        // 设置清理统计信息
        stats.setLastCleanupTime(lastCleanupTime);
        stats.setLastCleanupCount(lastCleanupCount);
        stats.setLastCleanupStrategy(lastCleanupStrategy);
        stats.setLastCleanupDuration(lastCleanupDuration);
        stats.setCleanupExecutionCount(cleanupExecutionCount.get());
        stats.setCleanupSuccessCount(cleanupSuccessCount.get());
        stats.setCleanupFailureCount(cleanupFailureCount.get());
        
        // 计算平均清理耗时
        if (cleanupExecutionCount.get() > 0) {
            stats.setAverageCleanupDuration(totalCleanupDuration.get() / cleanupExecutionCount.get());
        } else {
            stats.setAverageCleanupDuration(0);
        }
        
        return stats;
    }

    @Override
    public CleanupStats getPendingCleanupStats() {
        CleanupStats stats = new CleanupStats();
        
        // 获取总记录数
        stats.setTotalRecords(getTotalRecordCount());
        
        // 获取过期记录数（默认保留30天）
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
        stats.setExpiredRecords(agentConversationHistoryMapper.countByCreateTimeBefore(cutoffTime));
        
        return stats;
    }

    @Override
    public boolean validateCleanupConfig(CleanupStrategyEnum strategy, Object param) {
        if (strategy == null || param == null) {
            return false;
        }
        
        try {
            switch (strategy) {
                case TIME_BASED:
                case COUNT_BASED:
                case ACTIVITY_BASED:
                    int value = (Integer) param;
                    return value > 0;
                default:
                    return false;
            }
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * 获取总记录数
     */
    private int getTotalRecordCount() {
        // 使用一个较远的时间来统计所有记录
        LocalDateTime farPast = LocalDateTime.now().minusYears(100);
        return agentConversationHistoryMapper.countByCreateTimeBefore(farPast);
    }

    /**
     * 获取非活跃用户ID列表
     */
    private List<Long> getInactiveUserIds(int inactiveDays) {
        // 这里需要实现获取非活跃用户的逻辑
        // 由于项目结构限制，这里返回空列表，实际项目中需要根据用户活跃度表来实现
        log.warn("基于活跃度的清理功能需要用户活跃度表的支持，当前返回空列表");
        return List.of();
    }

    /**
     * 根据保留数量获取截止时间
     */
    private LocalDateTime getCutoffTimeForCount(int retentionCount) {
        try {
            // 获取第retentionCount条记录的创建时间
            List<AgentConversationHistory> records = agentConversationHistoryMapper.selectByCreateTimeBefore(
                LocalDateTime.now(), retentionCount);
            
            if (records.isEmpty()) {
                return null;
            }
            
            // 返回最早记录的创建时间
            return records.get(0).getCreateTime();
            
        } catch (Exception e) {
            log.error("获取截止时间失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除指定用户的对话历史
     */
    private int deleteConversationHistoryByUserId(Long userId) {
        try {
            agentConversationHistoryMapper.deleteByUserId(userId);
            // 由于deleteByUserId方法没有返回影响行数，这里需要统计删除前的记录数
            int countBefore = agentConversationHistoryMapper.countByUserId(userId);
            return countBefore; // 返回删除前的记录数作为影响行数
        } catch (Exception e) {
            log.error("删除用户 {} 的对话历史失败: {}", userId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取待清理记录数量
     */
    private int getPendingRecordsCount() {
        try {
            // 默认保留30天，统计过期记录数量
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
            return agentConversationHistoryMapper.countByCreateTimeBefore(cutoffTime);
        } catch (Exception e) {
            log.error("获取待清理记录数量失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 更新清理统计信息
     */
    private void updateStatistics(CleanupResult result, boolean success) {
        cleanupExecutionCount.incrementAndGet();
        
        if (success) {
            cleanupSuccessCount.incrementAndGet();
        } else {
            cleanupFailureCount.incrementAndGet();
        }
        
        totalCleanupDuration.addAndGet(result.getDuration());
        
        lastCleanupTime = result.getEndTime();
        lastCleanupCount = result.getAffectedRows();
        lastCleanupStrategy = result.getStrategy();
        lastCleanupDuration = result.getDuration();
    }
}