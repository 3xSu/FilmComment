package com.fc.job;

import com.fc.service.admin.PostAdminService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PostCleanupJob {

    @Autowired
    private PostAdminService postAdminService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 每天凌晨3点执行帖子自动清理任务
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行（与评论清理错开时间）
    public void autoCleanupExpiredPosts() {
        String lockKey = "lock:job:post:cleanup";
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;
        try {
            // 尝试获取锁，不等待，锁持有时间30分钟
            isLocked = lock.tryLock(0, 30, TimeUnit.MINUTES);
            if (!isLocked) {
                log.info("未获取到分布式锁，帖子清理任务将由其他实例执行");
                return;
            }

            log.info("成功获取分布式锁，开始执行帖子自动清理任务...");
            int cleanedCount = postAdminService.autoCleanupExpiredPosts();
            log.info("帖子自动清理任务完成，共清理 {} 条过期帖子", cleanedCount);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("帖子清理任务获取分布式锁时被中断", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("帖子清理任务分布式锁已释放");
            }
        }
    }

    /**
     * 测试用：每30分钟执行一次（开发环境使用）
     */
    // @Scheduled(cron = "0 */30 * * * ?")
    public void autoCleanupExpiredPostsForTest() {
        String lockKey = "lock:job:post:cleanup:test";
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(0, 10, TimeUnit.MINUTES);
            if (!isLocked) {
                log.info("测试环境：未获取到分布式锁，帖子清理任务将由其他实例执行");
                return;
            }

            log.info("测试环境：开始执行帖子自动清理任务...");
            int cleanedCount = postAdminService.autoCleanupExpiredPosts();
            log.info("测试环境：帖子自动清理任务完成，共清理 {} 条过期帖子", cleanedCount);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("测试环境：帖子清理任务获取分布式锁时被中断", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("测试环境：帖子清理任务分布式锁已释放");
            }
        }
    }
}