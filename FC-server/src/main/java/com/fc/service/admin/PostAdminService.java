package com.fc.service.admin;

import com.fc.config.PostAutoCleanupConfig;
import com.fc.vo.post.PostAdminVO;

/**
 * 管理员帖子管理服务接口
 */
public interface PostAdminService {

    /**
     * 删除帖子（逻辑删除）
     * @param postId 帖子ID
     * @return 被删除的帖子信息
     */
    PostAdminVO deletePost(Long postId);

    /**
     * 恢复被删除的帖子
     * @param postId 帖子ID
     * @return 恢复后的帖子信息
     */
    PostAdminVO restorePost(Long postId);

    /**
     * 物理删除帖子（谨慎使用）
     * @param postId 帖子ID
     */
    void permanentlyDeletePost(Long postId);

    /**
     * 自动清理过期帖子
     * @return 清理的帖子数量
     */
    int autoCleanupExpiredPosts();

    /**
     * 获取自动清理配置
     * @return 清理配置
     */
    PostAutoCleanupConfig getAutoCleanupConfig();

    /**
     * 更新自动清理配置
     * @param days 自动清理天数
     */
    void updateAutoCleanupConfig(Integer days);
}