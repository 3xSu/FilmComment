package com.fc.service.impl.agent;

import com.fc.dto.agent.CleanupResult;
import com.fc.dto.agent.CleanupStats;
import com.fc.enums.CleanupStrategyEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AgentConversationCleanupService 简化测试类
 * 
 * 测试AI Agent对话清理服务的基本功能，不依赖复杂的Mockito配置
 */
class AgentConversationCleanupServiceSimpleTest {

    @Test
    void testCleanupResultCreation_ShouldWorkCorrectly() {
        // 测试CleanupResult对象的创建和基本功能
        int affectedRows = 10;
        String strategy = CleanupStrategyEnum.TIME_BASED.name();
        
        CleanupResult result = new CleanupResult(affectedRows, strategy);
        
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(affectedRows);
        assertThat(result.getStrategy()).isEqualTo(strategy);
        assertThat(result.isSuccess()).isTrue();
        
        // 测试complete方法
        result.complete();
        assertThat(result.getDuration()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCleanupResultWithError_ShouldHandleCorrectly() {
        // 测试包含错误的CleanupResult
        int affectedRows = 0;
        String strategy = CleanupStrategyEnum.TIME_BASED.name();
        String errorMessage = "清理失败";
        
        CleanupResult result = new CleanupResult(affectedRows, strategy, false, errorMessage);
        
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(affectedRows);
        assertThat(result.getStrategy()).isEqualTo(strategy);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    void testCleanupStatsCreation_ShouldWorkCorrectly() {
        // 测试CleanupStats对象的创建和基本功能
        CleanupStats stats = new CleanupStats();
        
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalRecords()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getExpiredRecords()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getCleanupExecutionCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getCleanupSuccessCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getCleanupFailureCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getLastCleanupCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getAverageCleanupDuration()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCleanupStatsSetters_ShouldWorkCorrectly() {
        // 测试CleanupStats的setter方法
        CleanupStats stats = new CleanupStats();
        
        stats.setTotalRecords(100);
        stats.setExpiredRecords(25);
        stats.setCleanupExecutionCount(5);
        stats.setCleanupSuccessCount(4);
        stats.setCleanupFailureCount(1);
        stats.setLastCleanupCount(10);
        stats.setAverageCleanupDuration(150);
        stats.setLastCleanupTime(1234567890L);
        stats.setLastCleanupStrategy("TIME_BASED");
        stats.setLastCleanupDuration(200L);
        
        assertThat(stats.getTotalRecords()).isEqualTo(100);
        assertThat(stats.getExpiredRecords()).isEqualTo(25);
        assertThat(stats.getCleanupExecutionCount()).isEqualTo(5);
        assertThat(stats.getCleanupSuccessCount()).isEqualTo(4);
        assertThat(stats.getCleanupFailureCount()).isEqualTo(1);
        assertThat(stats.getLastCleanupCount()).isEqualTo(10);
        assertThat(stats.getAverageCleanupDuration()).isEqualTo(150);
        assertThat(stats.getLastCleanupTime()).isEqualTo(1234567890L);
        assertThat(stats.getLastCleanupStrategy()).isEqualTo("TIME_BASED");
        assertThat(stats.getLastCleanupDuration()).isEqualTo(200L);
    }

    @Test
    void testCleanupStrategyEnum_ShouldHaveCorrectValues() {
        // 测试CleanupStrategyEnum枚举值
        assertThat(CleanupStrategyEnum.TIME_BASED).isNotNull();
        assertThat(CleanupStrategyEnum.COUNT_BASED).isNotNull();
        assertThat(CleanupStrategyEnum.ACTIVITY_BASED).isNotNull();
        
        assertThat(CleanupStrategyEnum.valueOf("TIME_BASED")).isEqualTo(CleanupStrategyEnum.TIME_BASED);
        assertThat(CleanupStrategyEnum.valueOf("COUNT_BASED")).isEqualTo(CleanupStrategyEnum.COUNT_BASED);
        assertThat(CleanupStrategyEnum.valueOf("ACTIVITY_BASED")).isEqualTo(CleanupStrategyEnum.ACTIVITY_BASED);
    }

    @Test
    void testCleanupResultToString_ShouldNotBeNull() {
        // 测试CleanupResult的toString方法
        CleanupResult result = new CleanupResult(5, "TIME_BASED");
        result.complete();
        
        assertThat(result.toString()).isNotNull();
        assertThat(result.toString()).contains("affectedRows=5");
        assertThat(result.toString()).contains("strategy=TIME_BASED");
    }

    @Test
    void testCleanupStatsToString_ShouldNotBeNull() {
        // 测试CleanupStats的toString方法
        CleanupStats stats = new CleanupStats();
        stats.setTotalRecords(100);
        stats.setExpiredRecords(25);
        
        assertThat(stats.toString()).isNotNull();
        assertThat(stats.toString()).contains("totalRecords=100");
        assertThat(stats.toString()).contains("expiredRecords=25");
    }
}