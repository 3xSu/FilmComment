package com.fc.service.impl.user;

import com.fc.entity.Notification;
import com.fc.entity.User;
import com.fc.enums.RelatedType;
import com.fc.mapper.api.AccountMapper;
import com.fc.mapper.user.NotificationMapper;
import com.fc.result.PageResult;
import com.fc.service.user.NotificationService;
import com.fc.handler.NotificationWebSocketHandler;
import com.fc.vo.websocket.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    /**
     * 发送评论通知
     */
    @Override
    @Transactional
    public void sendCommentNotification(Long targetUserId, Long commenterId, Long postId, Long commentId, String content) {
        // 避免给自己发送通知
        if (targetUserId.equals(commenterId)) {
            return;
        }

        // 检查是否最近已发送过相同通知（避免重复通知）
        if (hasRecentDuplicateNotification(targetUserId, 1, commentId, 2, LocalDateTime.now().minusMinutes(1))) {
            log.debug("跳过重复的评论通知: targetUserId={}, commentId={}", targetUserId, commentId);
            return;
        }

        User commenter = accountMapper.getByUserId(commenterId);
        String commenterName = commenter != null ? commenter.getUsername() : "用户";

        // 截断过长的内容
        String truncatedContent = content.length() > 50 ? content.substring(0, 50) + "..." : content;

        Notification notification = Notification.builder()
                .userId(targetUserId)
                .type(1) // 评论通知
                .title("新的评论")
                .content(commenterName + " 评论了您的帖子: " + truncatedContent)
                .relatedId(commentId)
                .relatedType(RelatedType.COMMENT.getCode()) // 关联评论
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();

        saveAndSendNotification(notification);
        log.info("发送评论通知成功: targetUserId={}, commenterId={}", targetUserId, commenterId);
    }

    /**
     * 发送点赞通知
     */
    @Override
    @Transactional
    public void sendLikeNotification(Long targetUserId, Long likerId, Long postId, Long likeId) {
        // 避免给自己发送通知
        if (targetUserId.equals(likerId)) {
            log.debug("用户给自己点赞，不发送通知: userId={}, postId={}", likerId, postId);
            return;
        }

        // 检查是否最近已发送过相同通知（避免重复通知）
        if (hasRecentDuplicateNotification(targetUserId, 2, likeId, 1, LocalDateTime.now().minusMinutes(5))) {
            log.debug("跳过重复的点赞通知: targetUserId={}, likeId={}", targetUserId, likeId);
            return;
        }

        User liker = accountMapper.getByUserId(likerId);
        String likerName = liker != null ? liker.getUsername() : "用户";

        Notification notification = Notification.builder()
                .userId(targetUserId)
                .type(2) // 点赞通知
                .title("新的点赞")
                .content(likerName + " 点赞了您的帖子")
                .relatedId(postId) // 关联帖子ID
                .relatedType(RelatedType.POST.getCode()) // 关联类型：帖子
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();

        saveAndSendNotification(notification);
        log.info("发送点赞通知成功: targetUserId={}, likerId={}, postId={}",
                targetUserId, likerId, postId);
    }

    /**
     * 发送系统通知
     */
    @Override
    @Transactional
    public void sendSystemNotification(Long userId, String title, String content, Long relatedId, Integer relatedType) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(3) // 系统通知
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();

        saveAndSendNotification(notification);
        log.info("发送系统通知成功: userId={}, title={}", userId, title);
    }

    /**
     * 获取用户未读通知数量
     */
    @Override
    public Integer getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 获取用户通知列表
     */
    @Override
    public PageResult getNotifications(Long userId, LocalDateTime cursor, Integer size) {
        // 参数校验和默认值设置
        if (size == null || size < 1 || size > 100) {
            size = 20; // 限制每页大小
        }

        List<Notification> notifications = notificationMapper.selectByUserIdWithCursor(userId, cursor, size);

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setRecords(notifications);

        // 设置是否有下一页和下一个游标
        if (!notifications.isEmpty()) {
            // 获取最后一条记录的创建时间作为下一个游标
            Notification lastRecord = notifications.get(notifications.size() - 1);
            pageResult.setNextCursor(lastRecord.getCreateTime());
            pageResult.setHasNext(notifications.size() == size);
        } else {
            pageResult.setHasNext(false);
        }

        // 对于游标分页，不需要总记录数，设为-1表示未知
        pageResult.setTotal(-1);

        return pageResult;
    }


    /**
     * 标记通知为已读
     */
    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        int result = notificationMapper.markAsRead(id, userId);
        if (result > 0) {
            log.debug("标记通知为已读: id={}, userId={}", id, userId);
        }
    }

    /**
     * 标记所有通知为已读
     */
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        int result = notificationMapper.markAllAsRead(userId);
        log.info("标记所有通知为已读: userId={}, count={}", userId, result);
    }

    /**
     * 保存通知并发送WebSocket消息
     */
    private void saveAndSendNotification(Notification notification) {
        try {
            // 保存到数据库
            notificationMapper.insert(notification);

            // 通过WebSocket实时推送
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("NOTIFICATION")
                    .data(notification)
                    .timestamp(LocalDateTime.now())
                    .messageId(UUID.randomUUID().toString())
                    .build();

            webSocketHandler.sendMessageToUser(notification.getUserId(), message);

        } catch (Exception e) {
            log.error("保存或发送通知失败", e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 检查是否有重复通知
     */
    private boolean hasRecentDuplicateNotification(Long userId, Integer type, Long relatedId,
                                                   Integer relatedType, LocalDateTime startTime) {
        Integer count = notificationMapper.countRecentDuplicate(userId, type, relatedId, relatedType, startTime);
        return count != null && count > 0;
    }
}