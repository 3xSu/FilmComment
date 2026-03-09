package com.fc.service.impl.agent;

import com.fc.entity.AgentToolUsage;
import com.fc.mapper.agent.AgentToolUsageMapper;
import com.fc.service.agent.AgentToolUsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AI Agent工具调用记录服务实现类
 * 
 * 负责记录和监控AI Agent工具调用情况，包括执行时间、成功率、错误信息等
 */
@Slf4j
@Service
public class AgentToolUsageServiceImpl implements AgentToolUsageService {

    @Autowired
    private AgentToolUsageMapper agentToolUsageMapper;

    @Override
    @Transactional
    public void recordToolUsage(AgentToolUsage toolUsage) {
        try {
            toolUsage.setCreateTime(LocalDateTime.now());
            toolUsage.setUpdateTime(LocalDateTime.now());
            agentToolUsageMapper.insert(toolUsage);
            log.info("记录工具调用成功，工具名称: {}, 会话ID: {}, 执行时间: {}ms", 
                    toolUsage.getToolName(), toolUsage.getSessionId(), 
                    toolUsage.getExecutionTimeMs());
        } catch (Exception e) {
            log.error("记录工具调用失败，工具名称: {}, 错误: {}", 
                    toolUsage.getToolName(), e.getMessage(), e);
            throw new RuntimeException("记录工具调用失败", e);
        }
    }

    @Override
    public List<AgentToolUsage> getToolUsageBySessionId(String sessionId) {
        try {
            List<AgentToolUsage> toolUsageList = agentToolUsageMapper.selectBySessionId(sessionId);
            log.debug("获取工具调用记录成功，会话ID: {}, 记录数: {}", sessionId, toolUsageList.size());
            return toolUsageList;
        } catch (Exception e) {
            log.error("获取工具调用记录失败，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("获取工具调用记录失败", e);
        }
    }

    @Override
    public List<AgentToolUsage> getToolUsageByToolName(String toolName) {
        try {
            List<AgentToolUsage> toolUsageList = agentToolUsageMapper.selectByToolName(toolName);
            log.debug("获取工具调用记录成功，工具名称: {}, 记录数: {}", toolName, toolUsageList.size());
            return toolUsageList;
        } catch (Exception e) {
            log.error("获取工具调用记录失败，工具名称: {}, 错误: {}", toolName, e.getMessage(), e);
            throw new RuntimeException("获取工具调用记录失败", e);
        }
    }

    @Override
    public int countToolUsage(String toolName) {
        try {
            int count = agentToolUsageMapper.countByToolName(toolName);
            log.debug("统计工具调用次数成功，工具名称: {}, 次数: {}", toolName, count);
            return count;
        } catch (Exception e) {
            log.error("统计工具调用次数失败，工具名称: {}, 错误: {}", toolName, e.getMessage(), e);
            throw new RuntimeException("统计工具调用次数失败", e);
        }
    }

    @Override
    public double getToolSuccessRate(String toolName) {
        try {
            Double successRate = agentToolUsageMapper.getSuccessRateByToolName(toolName);
            double rate = successRate != null ? successRate : 0.0;
            log.debug("获取工具调用成功率成功，工具名称: {}, 成功率: {}", toolName, rate);
            return rate;
        } catch (Exception e) {
            log.error("获取工具调用成功率失败，工具名称: {}, 错误: {}", toolName, e.getMessage(), e);
            throw new RuntimeException("获取工具调用成功率失败", e);
        }
    }

    @Override
    public int getAverageExecutionTime(String toolName) {
        try {
            Integer avgTime = agentToolUsageMapper.getAverageExecutionTimeByToolName(toolName);
            int time = avgTime != null ? avgTime : 0;
            log.debug("获取工具平均执行时间成功，工具名称: {}, 时间: {}ms", toolName, time);
            return time;
        } catch (Exception e) {
            log.error("获取工具平均执行时间失败，工具名称: {}, 错误: {}", toolName, e.getMessage(), e);
            throw new RuntimeException("获取工具平均执行时间失败", e);
        }
    }

    @Override
    @Transactional
    public void deleteToolUsageBySessionId(String sessionId) {
        try {
            agentToolUsageMapper.deleteBySessionId(sessionId);
            log.info("删除工具调用记录成功，会话ID: {}", sessionId);
        } catch (Exception e) {
            log.error("删除工具调用记录失败，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("删除工具调用记录失败", e);
        }
    }

    @Override
    @Transactional
    public void cleanupOldToolUsage(int daysBefore) {
        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(daysBefore);
            String beforeTimeStr = beforeTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            agentToolUsageMapper.deleteBeforeTime(beforeTimeStr);
            log.info("清理旧工具调用记录成功，清理时间阈值: {}", beforeTimeStr);
        } catch (Exception e) {
            log.error("清理旧工具调用记录失败，错误: {}", e.getMessage(), e);
            throw new RuntimeException("清理旧工具调用记录失败", e);
        }
    }

    @Override
    public String getToolUsageStatistics() {
        try {
            StringBuilder statistics = new StringBuilder();
            statistics.append("工具使用统计信息:\n");
            
            // 获取常用工具列表（这里简化处理，实际应该从配置或枚举获取）
            String[] toolNames = {"movie_search", "history_recommend", "external_movie_search"};
            
            for (String toolName : toolNames) {
                int count = countToolUsage(toolName);
                double successRate = getToolSuccessRate(toolName);
                int avgTime = getAverageExecutionTime(toolName);
                
                statistics.append(String.format("- %s: 调用次数=%d, 成功率=%.2f%%, 平均执行时间=%dms\n", 
                        toolName, count, successRate * 100, avgTime));
            }
            
            log.debug("获取工具使用统计信息成功");
            return statistics.toString();
        } catch (Exception e) {
            log.error("获取工具使用统计信息失败，错误: {}", e.getMessage(), e);
            throw new RuntimeException("获取工具使用统计信息失败", e);
        }
    }
}