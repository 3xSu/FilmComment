package com.fc.service.admin;

import com.fc.config.CommentAutoCleanupConfig;
import com.fc.vo.comment.CommentAdminVO;

/**
 * 管理员评论管理服务接口
 */
public interface CommentAdminService {

    /**
     * 删除评论（逻辑删除）
     * @param commentId 评论ID
     * @return 被删除的评论信息
     */
    CommentAdminVO deleteComment(Long commentId);

    /**
     * 恢复被删除的评论
     * @param commentId 评论ID
     * @return 恢复后的评论信息
     */
    CommentAdminVO restoreComment(Long commentId);

    /**
     * 物理删除评论（谨慎使用）
     * @param commentId 评论ID
     */
    void permanentlyDeleteComment(Long commentId);

    /**
     * 自动清理过期评论
     * @return 清理的评论数量
     */
    int autoCleanupExpiredComments();

    /**
     * 获取自动清理配置
     * @return 清理配置
     */
    CommentAutoCleanupConfig getAutoCleanupConfig();

    /**
     * 更新自动清理配置
     * @param days 自动清理天数
     */
    void updateAutoCleanupConfig(Integer days);
}