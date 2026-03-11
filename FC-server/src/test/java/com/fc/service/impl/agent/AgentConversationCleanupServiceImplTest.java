package com.fc.service.impl.agent;

import com.fc.dto.agent.CleanupResult;
import com.fc.dto.agent.CleanupStats;
import com.fc.entity.AgentConversationHistory;
import com.fc.enums.CleanupStrategyEnum;
import com.fc.mapper.agent.AgentConversationHistoryMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AgentConversationCleanupServiceImpl 单元测试类
 * 
 * 测试AI Agent对话清理服务的各种清理策略和功能
 */
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "mockito.mock-maker-class=mock-maker-inline"
})
class AgentConversationCleanupServiceImplTest {

    @Mock
    private AgentConversationHistoryMapper agentConversationHistoryMapper;

    @InjectMocks
    private AgentConversationCleanupServiceImpl cleanupService;

    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        cleanupService = new AgentConversationCleanupServiceImpl();
        
        // 使用反射设置私有字段
        try {
            var mapperField = AgentConversationCleanupServiceImpl.class.getDeclaredField("agentConversationHistoryMapper");
            mapperField.setAccessible(true);
            mapperField.set(cleanupService, agentConversationHistoryMapper);
            
            var registryField = AgentConversationCleanupServiceImpl.class.getDeclaredField("meterRegistry");
            registryField.setAccessible(true);
            registryField.set(cleanupService, meterRegistry);
            
            // 调用初始化方法
            var initMethod = AgentConversationCleanupServiceImpl.class.getDeclaredMethod("initMonitoring");
            initMethod.setAccessible(true);
            initMethod.invoke(cleanupService);
        } catch (Exception e) {
            throw new RuntimeException("设置测试环境失败", e);
        }
    }

    @Test
    void testCleanupByTime_WithValidRetentionDays_ShouldSucceed() {
        // 准备测试数据
        int retentionDays = 30;
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        int expectedAffectedRows = 5;

        // 模拟Mapper行为 - 使用any()匹配时间参数
        when(agentConversationHistoryMapper.countByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(expectedAffectedRows);
        when(agentConversationHistoryMapper.deleteByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(expectedAffectedRows);

        // 执行测试
        CleanupResult result = cleanupService.cleanupByTime(retentionDays);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(expectedAffectedRows);
        assertThat(result.getStrategy()).isEqualTo(CleanupStrategyEnum.TIME_BASED.name());
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDuration()).isGreaterThan(0);

        // 验证方法调用 - 使用any()匹配时间参数
        verify(agentConversationHistoryMapper, times(1)).countByCreateTimeBefore(any(LocalDateTime.class));
        verify(agentConversationHistoryMapper, times(1)).deleteByCreateTimeBefore(any(LocalDateTime.class));
    }

    @Test
    void testCleanupByTime_WithNoRecordsToCleanup_ShouldReturnZero() {
        // 准备测试数据
        int retentionDays = 30;

        // 模拟没有需要清理的记录
        when(agentConversationHistoryMapper.countByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(0);

        // 执行测试
        CleanupResult result = cleanupService.cleanupByTime(retentionDays);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(0);
        assertThat(result.isSuccess()).isTrue();

        // 验证方法调用
        verify(agentConversationHistoryMapper, times(1)).countByCreateTimeBefore(any(LocalDateTime.class));
        verify(agentConversationHistoryMapper, never()).deleteByCreateTimeBefore(any());
    }

    @Test
    void testCleanupByTime_WithInvalidRetentionDays_ShouldFail() {
        // 准备测试数据
        int invalidRetentionDays = -1;

        // 执行测试
        CleanupResult result = cleanupService.cleanupByTime(invalidRetentionDays);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(0);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();

        // 验证方法没有被调用
        verify(agentConversationHistoryMapper, never()).countByCreateTimeBefore(any());
        verify(agentConversationHistoryMapper, never()).deleteByCreateTimeBefore(any());
    }

    @Test
    void testCleanupByCount_WithValidRetentionCount_ShouldSucceed() {
        // 准备测试数据
        int retentionCount = 1000;
        int totalCount = 1500;
        int expectedAffectedRows = 500;
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);

        // 模拟Mapper行为
        when(agentConversationHistoryMapper.countByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(totalCount);
        when(agentConversationHistoryMapper.selectByCreateTimeBefore(any(LocalDateTime.class), anyInt()))
            .thenReturn(List.of(createMockHistory(cutoffTime)));
        when(agentConversationHistoryMapper.deleteByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(expectedAffectedRows);

        // 执行测试
        CleanupResult result = cleanupService.cleanupByCount(retentionCount);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(expectedAffectedRows);
        assertThat(result.getStrategy()).isEqualTo(CleanupStrategyEnum.COUNT_BASED.name());
        assertThat(result.isSuccess()).isTrue();

        // 验证方法调用
        verify(agentConversationHistoryMapper, times(1)).countByCreateTimeBefore(any(LocalDateTime.class));
        verify(agentConversationHistoryMapper, times(1)).selectByCreateTimeBefore(any(LocalDateTime.class), anyInt());
        verify(agentConversationHistoryMapper, times(1)).deleteByCreateTimeBefore(any(LocalDateTime.class));
    }

    /**
     * 创建模拟的对话历史记录
     */
    private AgentConversationHistory createMockHistory(LocalDateTime createTime) {
        AgentConversationHistory history = new AgentConversationHistory();
        history.setCreateTime(createTime);
        return history;
    }

    @Test
    void testCleanupByCount_WithNoRecordsToCleanup_ShouldReturnZero() {
        // 准备测试数据
        int retentionCount = 1000;
        int totalCount = 800; // 小于保留数量

        // 模拟Mapper行为
        when(agentConversationHistoryMapper.countByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(totalCount);

        // 执行测试
        CleanupResult result = cleanupService.cleanupByCount(retentionCount);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(0);
        assertThat(result.isSuccess()).isTrue();

        // 验证方法调用
        verify(agentConversationHistoryMapper, times(1)).countByCreateTimeBefore(any(LocalDateTime.class));
        verify(agentConversationHistoryMapper, never()).deleteByCreateTimeBefore(any());
    }

    @Test
    void testCleanupByCount_WithInvalidRetentionCount_ShouldFail() {
        // 准备测试数据
        int invalidRetentionCount = -1;

        // 执行测试
        CleanupResult result = cleanupService.cleanupByCount(invalidRetentionCount);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(0);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();

        // 验证方法没有被调用
        verify(agentConversationHistoryMapper, never()).countByCreateTimeBefore(any());
        verify(agentConversationHistoryMapper, never()).deleteByCreateTimeBefore(any());
    }

    @Test
    void testExecuteAutoCleanup_ShouldUseDefaultStrategy() {
        // 准备测试数据
        int expectedAffectedRows = 10;

        // 模拟Mapper行为
        when(agentConversationHistoryMapper.countByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(expectedAffectedRows);
        when(agentConversationHistoryMapper.deleteByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(expectedAffectedRows);

        // 执行测试
        CleanupResult result = cleanupService.executeAutoCleanup();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(expectedAffectedRows);
        assertThat(result.getStrategy()).isEqualTo(CleanupStrategyEnum.TIME_BASED.name());
        assertThat(result.isSuccess()).isTrue();

        // 验证方法调用
        verify(agentConversationHistoryMapper, times(1)).countByCreateTimeBefore(any(LocalDateTime.class));
        verify(agentConversationHistoryMapper, times(1)).deleteByCreateTimeBefore(any(LocalDateTime.class));
    }

    @Test
    void testGetCleanupStats_ShouldReturnValidStatistics() {
        // 执行测试
        CleanupStats stats = cleanupService.getCleanupStats();

        // 验证结果
        assertThat(stats).isNotNull();
        assertThat(stats.getCleanupExecutionCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getCleanupSuccessCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getCleanupFailureCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getLastCleanupCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getAverageCleanupDuration()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCleanupByTime_WithDatabaseException_ShouldHandleGracefully() {
        // 准备测试数据
        int retentionDays = 30;

        // 模拟数据库异常
        when(agentConversationHistoryMapper.countByCreateTimeBefore(any(LocalDateTime.class))).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试
        CleanupResult result = cleanupService.cleanupByTime(retentionDays);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(0);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();

        // 验证方法调用
        verify(agentConversationHistoryMapper, times(1)).countByCreateTimeBefore(any(LocalDateTime.class));
        verify(agentConversationHistoryMapper, never()).deleteByCreateTimeBefore(any());
    }

    @Test
    void testCleanupByCount_WithNullCutoffTime_ShouldHandleGracefully() {
        // 准备测试数据
        int retentionCount = 1000;
        int totalCount = 1500;

        // 模拟无法确定截止时间
        when(agentConversationHistoryMapper.countByCreateTimeBefore(any(LocalDateTime.class))).thenReturn(totalCount);

        // 执行测试
        CleanupResult result = cleanupService.cleanupByCount(retentionCount);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getAffectedRows()).isEqualTo(0);
        assertThat(result.isSuccess()).isTrue();

        // 验证方法调用
        verify(agentConversationHistoryMapper, times(1)).countByCreateTimeBefore(any(LocalDateTime.class));
        verify(agentConversationHistoryMapper, never()).deleteByCreateTimeBefore(any());
    }

    @Test
    void testMonitoringMetrics_ShouldBeRegistered() {
        // 验证监控指标是否已注册
        assertThat(meterRegistry.find("agent.cleanup.time").timer()).isNotNull();
        assertThat(meterRegistry.find("agent.cleanup.records").counter()).isNotNull();
        assertThat(meterRegistry.find("agent.cleanup.executions").counter()).isNotNull();
        assertThat(meterRegistry.find("agent.cleanup.failures").counter()).isNotNull();
        assertThat(meterRegistry.find("agent.cleanup.pending").gauge()).isNotNull();
    }
}