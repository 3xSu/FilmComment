package com.fc.service.user;

import com.fc.vo.post.PostStatVO;

/**
 * 帖子统计服务接口
 */
public interface PostStatService {

    /**
     * 增加帖子浏览量
     */
    void incrementViewCount(Long postId);

    /**
     * 更新帖子点赞数
     */
    void updateLikeCount(Long postId, Integer likeCount);

    /**
     * 更新帖子评论数
     */
    void updateCommentCount(Long postId, Integer commentCount);

    /**
     * 获取帖子统计信息
     */
    PostStatVO getPostStats(Long postId);

    /**
     * 广播帖子统计更新
     */
    void broadcastPostStatUpdate(Long postId);
}