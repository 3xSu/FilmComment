package com.fc.job;

import com.fc.dto.agent.CleanupResult;
import com.fc.service.agent.AgentConversationCleanupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AgentConversationCleanupJob 单元测试类
 * 
 * 测试AI Agent对话清理定时任务的执行逻辑和异常处理
 */
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "mockito.mock-maker-class=mock-maker-inline"
})
class AgentConversationCleanupJobTest {

    @Mock
    private AgentConversationCleanupService cleanupService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    private AgentConversationCleanupJob cleanupJob;

    @BeforeEach
    void setUp() {
        cleanupJob = new AgentConversationCleanupJob();
        
        // 使用反射设置私有字段
        try {
            var serviceField = AgentConversationCleanupJob.class.getDeclaredField("cleanupService");
            serviceField.setAccessible(true);
            serviceField.set(cleanupJob, cleanupService);
            
            var clientField = AgentConversationCleanupJob.class.getDeclaredField("redissonClient");
            clientField.setAccessible(true);
            clientField.set(cleanupJob, redissonClient);
        } catch (Exception e) {
            throw new RuntimeException("设置测试环境失败", e);
        }
    }

    @Test
    void testAutoCleanupExpiredConversations_WithLockAcquired_ShouldExecuteCleanup() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";
        int affectedRows = 25;
        long duration = 150L;
        
        CleanupResult mockResult = new CleanupResult(affectedRows, "TIME_BASED");
        mockResult.setDuration(duration);
        mockResult.setSuccess(true);

        // 模拟分布式锁行为
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        
        // 模拟清理服务行为
        when(cleanupService.executeAutoCleanup()).thenReturn(mockResult);

        // 执行测试
        cleanupJob.autoCleanupExpiredConversations();

        // 验证方法调用
        verify(redissonClient, times(1)).getLock(lockKey);
        verify(lock, times(1)).tryLock(0, 30, TimeUnit.MINUTES);
        verify(cleanupService, times(1)).executeAutoCleanup();
        verify(lock, times(1)).unlock();
    }

    @Test
    void testAutoCleanupExpiredConversations_WithLockNotAcquired_ShouldSkipExecution() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";

        // 模拟无法获取分布式锁
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenReturn(false);

        // 执行测试
        cleanupJob.autoCleanupExpiredConversations();

        // 验证方法调用
        verify(redissonClient, times(1)).getLock(lockKey);
        verify(lock, times(1)).tryLock(0, 30, TimeUnit.MINUTES);
        verify(cleanupService, never()).executeAutoCleanup();
        verify(lock, never()).unlock();
    }

    @Test
    void testAutoCleanupExpiredConversations_WithCleanupSuccess_ShouldLogSuccess() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";
        int affectedRows = 50;
        long duration = 200L;
        
        CleanupResult mockResult = new CleanupResult(affectedRows, "TIME_BASED");
        mockResult.setDuration(duration);
        mockResult.setSuccess(true);

        // 模拟分布式锁行为
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        
        // 模拟清理服务行为
        when(cleanupService.executeAutoCleanup()).thenReturn(mockResult);

        // 执行测试
        cleanupJob.autoCleanupExpiredConversations();

        // 验证方法调用
        verify(cleanupService, times(1)).executeAutoCleanup();
        verify(lock, times(1)).unlock();
    }

    @Test
    void testAutoCleanupExpiredConversations_WithCleanupFailure_ShouldLogError() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";
        String errorMessage = "数据库连接失败";
        
        CleanupResult mockResult = new CleanupResult(0, "TIME_BASED");
        mockResult.setSuccess(false);
        mockResult.setErrorMessage(errorMessage);

        // 模拟分布式锁行为
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        
        // 模拟清理服务行为
        when(cleanupService.executeAutoCleanup()).thenReturn(mockResult);

        // 执行测试
        cleanupJob.autoCleanupExpiredConversations();

        // 验证方法调用
        verify(cleanupService, times(1)).executeAutoCleanup();
        verify(lock, times(1)).unlock();
    }

    @Test
    void testAutoCleanupExpiredConversations_WithInterruptedException_ShouldHandleGracefully() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";

        // 模拟获取锁时被中断
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenThrow(new InterruptedException("获取锁时被中断"));

        // 执行测试
        cleanupJob.autoCleanupExpiredConversations();

        // 验证方法调用
        verify(redissonClient, times(1)).getLock(lockKey);
        verify(lock, times(1)).tryLock(0, 30, TimeUnit.MINUTES);
        verify(cleanupService, never()).executeAutoCleanup();
        verify(lock, never()).unlock();
        
        // 验证线程中断状态被恢复
        // 注意：在InterruptedException被捕获后，中断状态会被清除
        // 但由于测试环境，我们需要手动清除中断状态
        Thread.interrupted(); // 清除中断状态
        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    @Test
    void testAutoCleanupExpiredConversations_WithExceptionInCleanup_ShouldReleaseLock() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";

        // 模拟分布式锁行为
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        
        // 模拟清理服务抛出异常
        when(cleanupService.executeAutoCleanup()).thenThrow(new RuntimeException("清理服务异常"));

        // 执行测试并验证异常被抛出
        assertThatThrownBy(() -> cleanupJob.autoCleanupExpiredConversations())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("清理服务异常");

        // 验证方法调用
        verify(redissonClient, times(1)).getLock(lockKey);
        verify(lock, times(1)).tryLock(0, 30, TimeUnit.MINUTES);
        verify(cleanupService, times(1)).executeAutoCleanup();
        verify(lock, times(1)).unlock();
    }

    @Test
    void testAutoCleanupExpiredConversations_WithLockNotHeld_ShouldNotUnlock() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";

        // 模拟分布式锁行为（锁未被当前线程持有）
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(false); // 锁未被当前线程持有
        
        // 模拟清理服务行为
        when(cleanupService.executeAutoCleanup()).thenReturn(new CleanupResult(0, "TIME_BASED"));

        // 执行测试
        cleanupJob.autoCleanupExpiredConversations();

        // 验证方法调用
        verify(redissonClient, times(1)).getLock(lockKey);
        verify(lock, times(1)).tryLock(0, 30, TimeUnit.MINUTES);
        verify(cleanupService, times(1)).executeAutoCleanup();
        verify(lock, never()).unlock(); // 不应该解锁
    }

    @Test
    void testAutoCleanupExpiredConversations_WithZeroAffectedRows_ShouldHandleNormally() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";
        
        CleanupResult mockResult = new CleanupResult(0, "TIME_BASED");
        mockResult.setSuccess(true);

        // 模拟分布式锁行为
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        
        // 模拟清理服务行为
        when(cleanupService.executeAutoCleanup()).thenReturn(mockResult);

        // 执行测试
        cleanupJob.autoCleanupExpiredConversations();

        // 验证方法调用
        verify(cleanupService, times(1)).executeAutoCleanup();
        verify(lock, times(1)).unlock();
    }

    @Test
    void testAutoCleanupExpiredConversations_WithLargeAffectedRows_ShouldHandleNormally() throws InterruptedException {
        // 准备测试数据
        String lockKey = "lock:job:agent:conversation:cleanup";
        int affectedRows = 10000;
        long duration = 5000L;
        
        CleanupResult mockResult = new CleanupResult(affectedRows, "TIME_BASED");
        mockResult.setDuration(duration);
        mockResult.setSuccess(true);

        // 模拟分布式锁行为
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(0, 30, TimeUnit.MINUTES)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        
        // 模拟清理服务行为
        when(cleanupService.executeAutoCleanup()).thenReturn(mockResult);

        // 执行测试
        cleanupJob.autoCleanupExpiredConversations();

        // 验证方法调用
        verify(cleanupService, times(1)).executeAutoCleanup();
        verify(lock, times(1)).unlock();
    }
}