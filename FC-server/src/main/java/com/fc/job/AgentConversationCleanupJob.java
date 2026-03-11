package com.fc.job;

import com.fc.dto.agent.CleanupResult;
import com.fc.service.agent.AgentConversationCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * AI Agent对话历史清理定时任务
 * 
 * 负责定期清理过期的AI Agent对话历史数据，防止数据无限增长
 * 使用分布式锁确保在集群环境中只有一个实例执行清理任务
 */
@Component
@Slf4j
public class AgentConversationCleanupJob {
    
    @Autowired
    private AgentConversationCleanupService cleanupService;
    
    @Autowired
    private RedissonClient redissonClient;
    
    /**
     * 每天凌晨4点执行自动清理任务
     * 与评论清理（2点）和帖子清理（3点）错开时间，避免资源竞争
     */
    @Scheduled(cron = "0 0 4 * * ?") // 每天凌晨4点执行
    public void autoCleanupExpiredConversations() {
        String lockKey = "lock:job:agent:conversation:cleanup";
        RLock lock = redissonClient.getLock(lockKey);
        
        boolean isLocked = false;
        try {
            // 尝试获取锁，不等待，锁持有时间30分钟
            isLocked = lock.tryLock(0, 30, TimeUnit.MINUTES);
            if (!isLocked) {
                log.info("未获取到分布式锁，AI Agent对话清理任务将由其他实例执行");
                return;
            }
            
            log.info("成功获取分布式锁，开始执行AI Agent对话自动清理任务...");
            try {
                CleanupResult result = cleanupService.executeAutoCleanup();
                
                if (result.isSuccess()) {
                    log.info("AI Agent对话自动清理任务完成，共清理 {} 条记录，耗时 {} 毫秒", 
                            result.getAffectedRows(), result.getDuration());
                } else {
                    log.error("AI Agent对话自动清理任务执行失败: {}", result.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("AI Agent对话自动清理任务执行过程中发生异常: {}", e.getMessage(), e);
                throw e; // 重新抛出异常以便测试可以捕获
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("AI Agent对话清理任务获取分布式锁时被中断", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("AI Agent对话清理任务分布式锁已释放");
            }
        }
    }
}