package com.fc.service.impl.admin;

import com.fc.config.CommentAutoCleanupConfig;
import com.fc.constant.MessageConstant;
import com.fc.entity.Comment;
import com.fc.entity.User;
import com.fc.exception.CommentNotFoundException;
import com.fc.mapper.api.AccountMapper;
import com.fc.mapper.admin.CommentAdminMapper;
import com.fc.service.admin.CommentAdminService;
import com.fc.vo.comment.CommentAdminVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentAdminServiceImpl implements CommentAdminService {

    @Autowired
    private CommentAdminMapper commentAdminMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Value("${comment.auto-cleanup.days:7}")
    private Integer autoCleanupDays;

    @Value("${comment.auto-cleanup.enabled:true}")
    private Boolean autoCleanupEnabled;

    /**
     * 删除评论（逻辑删除）
     */
    @Override
    @Transactional
    public CommentAdminVO deleteComment(Long commentId) {
        Comment comment = commentAdminMapper.getByCommentId(commentId);
        if (comment == null) {
            throw new CommentNotFoundException(MessageConstant.COMMENT_NOT_FOUND);
        }

        if (comment.getIsDeleted() == 1) {
            return buildCommentAdminVO(comment);
        }

        // 逻辑删除并记录删除时间
        Comment commentToUpdate = Comment.builder()
                .commentId(commentId)
                .isDeleted(1)
                .deleteTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        commentAdminMapper.update(commentToUpdate);

        Comment updatedComment = commentAdminMapper.getByCommentId(commentId);
        return buildCommentAdminVO(updatedComment);
    }

    /**
     * 恢复被删除的评论
     */
    @Override
    @Transactional
    public CommentAdminVO restoreComment(Long commentId) {
        Comment comment = commentAdminMapper.getByCommentId(commentId);
        if (comment == null) {
            throw new CommentNotFoundException(MessageConstant.COMMENT_NOT_FOUND);
        }

        if (comment.getIsDeleted() == 0) {
            return buildCommentAdminVO(comment);
        }

        // 恢复评论并清除删除时间
        Comment commentToUpdate = Comment.builder()
                .commentId(commentId)
                .isDeleted(0)
                .deleteTime(null)
                .updateTime(LocalDateTime.now())
                .build();

        commentAdminMapper.update(commentToUpdate);

        Comment updatedComment = commentAdminMapper.getByCommentId(commentId);
        return buildCommentAdminVO(updatedComment);
    }

    /**
     * 手动物理删除评论
     */
    @Override
    @Transactional
    public void permanentlyDeleteComment(Long commentId) {
        Comment comment = commentAdminMapper.getByCommentId(commentId);
        if (comment == null) {
            throw new CommentNotFoundException(MessageConstant.COMMENT_NOT_FOUND);
        }

        // 1. 先删除评论图片
        commentAdminMapper.deleteCommentImagesByCommentId(commentId);
        log.info("删除评论图片: commentId={}", commentId);

        // 2. 再删除评论
        commentAdminMapper.delete(commentId);
        log.info("物理删除评论: commentId={}", commentId);
    }

    /**
     * 自动清理过期评论
     */
    @Override
    @Transactional
    public int autoCleanupExpiredComments() {
        if (!autoCleanupEnabled) {
            return 0;
        }

        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(autoCleanupDays);
        List<Comment> commentsToCleanup = commentAdminMapper.getCommentsToCleanup(thresholdTime);

        if (commentsToCleanup.isEmpty()) {
            return 0;
        }

        List<Long> commentIds = commentsToCleanup.stream()
                .map(Comment::getCommentId)
                .collect(Collectors.toList());

        // 1. 先批量删除评论图片
        commentAdminMapper.batchDeleteCommentImages(commentIds);
        log.info("批量删除评论图片: 数量={}", commentIds.size());

        // 2. 再批量删除评论
        commentAdminMapper.batchDeleteComments(commentIds);
        log.info("批量物理删除评论: 数量={}", commentIds.size());

        return commentIds.size();
    }

    /**
     * 获取自动清理配置
     */
    @Override
    public CommentAutoCleanupConfig getAutoCleanupConfig() {
        // 计算下次清理时间（明天凌晨2点）
        LocalDateTime nextCleanup = LocalDateTime.now()
                .plusDays(1)
                .withHour(2)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        return CommentAutoCleanupConfig.builder()
                .autoCleanupDays(autoCleanupDays)
                .enabled(autoCleanupEnabled)
                .nextCleanupTime(nextCleanup.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    /**
     * 更新自动清理配置
     */
    @Override
    public void updateAutoCleanupConfig(Integer days) {
        if (days != null && days > 0) {
            this.autoCleanupDays = days;
        }
    }

    /**
     * 构建CommentAdminVO对象
     */
    private CommentAdminVO buildCommentAdminVO(Comment comment) {
        // 查询用户信息
        String username = "用户";
        String avatarUrl = "/default-avatar.png";
        try {
            User user = accountMapper.getByUserId(comment.getUserId());
            if (user != null) {
                username = user.getUsername() != null ? user.getUsername() : "用户" + comment.getUserId();
                avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : "/default-avatar.png";
            } else {
                username = "用户" + comment.getUserId();
            }
        } catch (Exception e) {
            username = "用户" + comment.getUserId();
        }

        // 查询回复数量
        Integer replyCount = 0;
        try {
            if (comment.getParentId() == 0) {
                replyCount = commentAdminMapper.getReplyCount(comment.getCommentId());
            }
        } catch (Exception e) {
            // 忽略异常
        }

        // 构建管理专用的VO，包含所有管理字段
        return CommentAdminVO.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUserId())
                .username(username)
                .avatarUrl(avatarUrl)
                .postId(comment.getPostId())
                .postTitle("") // 可以根据需要查询帖子标题
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0)
                .replyCount(replyCount)
                .createTime(comment.getCreateTime())
                .deleteTime(comment.getDeleteTime()) // 删除时间
                .isDeleted(comment.getIsDeleted())   // 删除状态
                .updateTime(comment.getUpdateTime()) // 更新时间
                .liked(false) // 管理员接口不关注点赞状态
                .build();
    }
}