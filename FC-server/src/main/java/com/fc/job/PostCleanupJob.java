package com.fc.job;

import com.fc.service.admin.PostAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostCleanupJob {

    @Autowired
    private PostAdminService postAdminService;

    /**
     * 每天凌晨3点执行帖子自动清理任务
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行（与评论清理错开时间）
    public void autoCleanupExpiredPosts() {
        try {
            log.info("开始执行帖子自动清理任务...");
            int cleanedCount = postAdminService.autoCleanupExpiredPosts();
            log.info("帖子自动清理任务完成，共清理 {} 条过期帖子", cleanedCount);
        } catch (Exception e) {
            log.error("帖子自动清理任务执行失败", e);
        }
    }

    /**
     * 测试用：每30分钟执行一次（开发环境使用）
     */
    // @Scheduled(cron = "0 */30 * * * ?")
    public void autoCleanupExpiredPostsForTest() {
        try {
            log.info("测试环境：开始执行帖子自动清理任务...");
            int cleanedCount = postAdminService.autoCleanupExpiredPosts();
            log.info("测试环境：帖子自动清理任务完成，共清理 {} 条过期帖子", cleanedCount);
        } catch (Exception e) {
            log.error("测试环境：帖子自动清理任务执行失败", e);
        }
    }
}