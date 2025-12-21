package com.fc.job;

import com.fc.service.admin.CommentAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommentCleanupJob {

    @Autowired
    private CommentAdminService commentAdminService;

    /**
     * 每天凌晨2点执行自动清理任务
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void autoCleanupExpiredComments() {
        try {
            log.info("开始执行评论自动清理任务...");
            int cleanedCount = commentAdminService.autoCleanupExpiredComments();
            log.info("评论自动清理任务完成，共清理 {} 条过期评论", cleanedCount);
        } catch (Exception e) {
            log.error("评论自动清理任务执行失败", e);
        }
    }

    /**
     * 测试用：每10分钟执行一次（开发环境使用）
     */
    // @Scheduled(cron = "0 */10 * * * ?")
    public void autoCleanupExpiredCommentsForTest() {
        try {
            log.info("测试环境：开始执行评论自动清理任务...");
            int cleanedCount = commentAdminService.autoCleanupExpiredComments();
            log.info("测试环境：评论自动清理任务完成，共清理 {} 条过期评论", cleanedCount);
        } catch (Exception e) {
            log.error("测试环境：评论自动清理任务执行失败", e);
        }
    }
}