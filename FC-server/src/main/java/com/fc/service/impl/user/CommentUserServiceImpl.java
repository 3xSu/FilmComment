package com.fc.service.impl.user;

import com.fc.context.BaseContext;
import com.fc.dto.comment.CommentPublishDTO;
import com.fc.entity.Comment;
import com.fc.entity.CommentImages;
import com.fc.entity.Post;
import com.fc.entity.User;
import com.fc.mapper.user.CommentUserMapper;
import com.fc.mapper.api.AccountMapper;
import com.fc.mapper.user.PostUserMapper;
import com.fc.service.user.CommentUserService;
import com.fc.service.user.NotificationService;
import com.fc.service.user.PostStatService;
import com.fc.utils.AliOssUtil;
import com.fc.utils.FileSecurityValidator;
import com.fc.vo.comment.CommentImageUploadVO;
import com.fc.vo.comment.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CommentUserServiceImpl implements CommentUserService {

    @Autowired
    private CommentUserMapper commentUserMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PostUserMapper postUserMapper;

    @Autowired
    private PostStatService postStatService;

    /**
     * 发表评论
     * @param commentPublishDTO 评论信息
     * @return 评论信息
     */
    @Override
    @Transactional
    public CommentVO publishComment(CommentPublishDTO commentPublishDTO) {
        Long userId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        Long postId = commentPublishDTO.getPostId();

        try {
            Long parentId = commentPublishDTO.getParentId() != null ? commentPublishDTO.getParentId() : 0L;
            // 如果是回复评论，需要检查父评论是否存在
            if (parentId > 0) {
                Comment parentComment = commentUserMapper.getByCommentId(parentId);
                if (parentComment == null || parentComment.getIsDeleted() == 1) {
                    throw new RuntimeException("父评论不存在或已被删除");
                }
                // 确保回复的是同一个帖子的评论
                if (!parentComment.getPostId().equals(commentPublishDTO.getPostId())) {
                    throw new RuntimeException("评论帖子ID不匹配");
                }
            }

            // 创建评论实体
            Comment comment = Comment.builder()
                    .userId(userId)
                    .postId(commentPublishDTO.getPostId())
                    .parentId(parentId)
                    .content(commentPublishDTO.getContent())
                    .likeCount(0)
                    .isDeleted(0)
                    .createTime(now)
                    .updateTime(now)
                    .build();

            // 插入评论
            commentUserMapper.insert(comment);

            // 更新帖子评论数（新增功能）
            updatePostCommentCount(postId);

            // 发送评论通知给帖子作者
            sendCommentNotificationToPostAuthor(
                    commentPublishDTO.getPostId(),
                    userId,
                    comment.getCommentId(),
                    commentPublishDTO.getContent()
            );

            // 构建返回结果
            return buildCommentVO(comment);

        } catch (Exception e) {
            log.error("评论发布失败", e);
            sendCommentPublishFailedNotification(userId, e.getMessage());
            throw new RuntimeException("评论发布失败: " + e.getMessage(), e);
        }
    }



    /**
     * 删除评论
     * @param commentId 评论ID
     */
    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();

        // 查询评论是否存在且属于当前用户
        Comment comment = commentUserMapper.getByCommentId(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在或已被删除");
        }

        Long postId = comment.getPostId();

        // 检查评论是否属于当前用户
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }

        // 执行逻辑删除
        int result = commentUserMapper.deleteComment(commentId, userId, now, now);
        if (result == 0) {
            throw new RuntimeException("评论删除失败");
        }

        updatePostCommentCount(postId);
        log.info("评论删除成功，更新帖子评论数: commentId={}, postId={}", commentId, postId);
    }

    /**
     * 评论发布后更新帖子评论数（新增方法）
     */
    private void updatePostCommentCount(Long postId) {
        try {
            // 获取当前评论数（包括未删除的评论）
            Integer commentCount = commentUserMapper.countCommentsByPostId(postId);

            // 实时更新评论数
            postStatService.updateCommentCount(postId, commentCount);

            log.debug("更新帖子评论数成功: postId={}, commentCount={}", postId, commentCount);
        } catch (Exception e) {
            log.error("更新帖子评论数失败: postId={}", postId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 上传评论图片
     * @param commentId 评论ID
     * @param imageFile 图片文件
     * @param sortOrder 图片排序
     * @return 图片上传信息
     */
    @Override
    @Transactional
    public CommentImageUploadVO uploadCommentImage(Long commentId, MultipartFile imageFile, Integer sortOrder) {
        log.info("用户上传评论图片: commentId={}, sortOrder={}", commentId, sortOrder);

        // 检查评论是否存在且属于当前用户
        Comment comment = commentUserMapper.getByCommentId(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new RuntimeException("评论不存在或已被删除");
        }

        // 检查用户是否有权限操作（只能操作自己创建的评论）
        Long currentUserId = BaseContext.getCurrentId();
        if (!comment.getUserId().equals(currentUserId)) {
            throw new RuntimeException("无权操作他人评论");
        }

        // 检查文件是否为空
        if (imageFile == null || imageFile.isEmpty()) {
            throw new RuntimeException("图片文件不能为空");
        }

        // 检查文件类型
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名无效");
        }

        String extension = getSafeExtension(imageFile);
        if (!isImageFile(extension)) {
            throw new RuntimeException("不支持的文件类型，仅支持jpg、jpeg、png格式");
        }

        // 检查文件大小（限制为5MB）
        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过5MB");
        }

        try {
            // 安全文件验证
            FileSecurityValidator.validateImageFile(imageFile);
            // 生成唯一的文件名
            String fileName = generateImageFileName(extension);

            // 上传到阿里云OSS
            String imageUrl = aliOssUtil.upload(imageFile.getBytes(), fileName);

            // 创建评论图片实体
            CommentImages commentImage = CommentImages.builder()
                    .commentId(commentId)
                    .imageUrl(imageUrl)
                    .sortOrder(sortOrder != null ? sortOrder : 1)
                    .createTime(LocalDateTime.now())
                    .build();

            // 插入数据库
            commentUserMapper.insertCommentImage(commentImage);

            // 构建返回结果
            return CommentImageUploadVO.builder()
                    .imageId(commentImage.getId())
                    .commentId(commentId)
                    .imageUrl(imageUrl)
                    .sortOrder(commentImage.getSortOrder())
                    .createTime(commentImage.getCreateTime())
                    .build();

        } catch (IOException e) {
            log.error("评论图片上传失败: commentId={}", commentId, e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 安全获取文件扩展名
     * @param file
     * @return
     */
    private String getSafeExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return ".jpg"; // 默认扩展名
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 只允许特定的扩展名
        List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            return ".jpg"; // 强制使用安全扩展名
        }

        return extension;
    }

    /**
     * 检查是否为图片文件
     * @param extension 文件扩展名
     * @return 是否为图片文件
     */
    private boolean isImageFile(String extension) {
        if (extension == null) return false;

        String lowerExtension = extension.toLowerCase();
        return lowerExtension.equals(".jpg") ||
                lowerExtension.equals(".jpeg") ||
                lowerExtension.equals(".png");
    }

    /**
     * 生成唯一的图片文件名
     * @param extension 文件扩展名
     * @return 文件名
     */
    private String generateImageFileName(String extension) {
        return "comment-images/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 发送评论通知给帖子作者
     */
    private void sendCommentNotificationToPostAuthor(Long postId, Long commenterId, Long commentId, String content) {
        try {
            Post post = postUserMapper.getByPostId(postId);
            if (post != null && !post.getUserId().equals(commenterId)) {
                notificationService.sendCommentNotification(
                        post.getUserId(),
                        commenterId,
                        postId,
                        commentId,
                        content
                );
            }
        } catch (Exception e) {
            log.error("发送评论通知异常", e);
        }
    }

    /**
     * 评论发布失败时发送系统通知
     */
    private void sendCommentPublishFailedNotification(Long userId, String errorMessage) {
        try {
            notificationService.sendSystemNotification(
                    userId,
                    "评论发布失败",
                    "您的评论发布失败，原因：" + errorMessage,
                    null,
                    0
            );
        } catch (Exception e) {
            log.error("发送评论发布失败通知异常", e);
        }
    }

    /**
     * 构建CommentVO对象
     * @param comment 评论实体
     * @return CommentVO
     */
    private CommentVO buildCommentVO(Comment comment) {
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
            // 如果是评论帖子，查询回复数量；如果是回复评论，不查询回复数量（避免嵌套过深）
            if (comment.getParentId() == 0) {
                replyCount = commentUserMapper.getReplyCount(comment.getCommentId());
            }
        } catch (Exception e) {
            // 忽略异常，使用默认值
        }

        return CommentVO.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUserId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .username(username)
                .avatarUrl(avatarUrl)
                .content(comment.getContent())
                .likeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0)
                .replyCount(replyCount)
                .createTime(comment.getCreateTime())
                .liked(false) // 默认未点赞
                .build();
    }

}
