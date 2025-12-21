package com.fc.service.user;

import com.fc.entity.Notification;
import com.fc.result.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 发送评论通知
     * @param targetUserId 目标用户ID
     * @param commenterId 评论者ID
     * @param postId 帖子ID
     * @param commentId 评论ID
     * @param content 评论内容
     */
    void sendCommentNotification(Long targetUserId, Long commenterId, Long postId, Long commentId, String content);

    /**
     * 发送点赞通知
     * @param targetUserId 目标用户ID
     * @param likerId 点赞者ID
     * @param postId 帖子ID
     * @param likeId 点赞ID
     */
    void sendLikeNotification(Long targetUserId, Long likerId, Long postId, Long likeId);

    /**
     * 发送系统通知
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     */
    void sendSystemNotification(Long userId, String title, String content, Long relatedId, Integer relatedType);

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 获取用户通知列表
     * @param userId 用户ID
     * @param cursor
     * @param size   每页大小
     * @return 通知列表
     */
    PageResult getNotifications(Long userId, LocalDateTime cursor, Integer size);

    /**
     * 标记通知为已读
     * @param id 通知ID
     * @param userId 用户ID
     */
    void markAsRead(Long id, Long userId);

    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);
}