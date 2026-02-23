package com.fc.service.impl.admin;

import com.fc.config.PostAutoCleanupConfig;
import com.fc.constant.MessageConstant;
import com.fc.entity.Post;
import com.fc.entity.User;
import com.fc.exception.PostNotFoundException;
import com.fc.mapper.admin.CommentAdminMapper;
import com.fc.mapper.api.AccountMapper;
import com.fc.mapper.admin.PostAdminMapper;
import com.fc.service.admin.PostAdminService;
import com.fc.vo.post.PostAdminVO;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostAdminServiceImpl implements PostAdminService {

    @Autowired
    private PostAdminMapper postAdminMapper;

    @Autowired
    private CommentAdminMapper commentAdminMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Value("${post.auto-cleanup.days:30}")
    private Integer autoCleanupDays;

    @Value("${post.auto-cleanup.enabled:true}")
    private Boolean autoCleanupEnabled;

    /**
     * 删除帖子（逻辑删除）
     */
    @Override
    @Transactional
    public PostAdminVO deletePost(Long postId) {
        Post post = postAdminMapper.getByPostId(postId);
        if (post == null) {
            throw new PostNotFoundException(MessageConstant.POST_NOT_FOUND);
        }

        if (post.getIsDeleted() == 1) {
            return buildPostAdminVO(post);
        }

        // 逻辑删除并记录删除时间
        Post postToUpdate = Post.builder()
                .postId(postId)
                .isDeleted(1)
                .deleteTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        postAdminMapper.update(postToUpdate);

        Post updatedPost = postAdminMapper.getByPostId(postId);
        return buildPostAdminVO(updatedPost);
    }

    /**
     * 恢复被删除的帖子
     */
    @Override
    @Transactional
    public PostAdminVO restorePost(Long postId) {
        Post post = postAdminMapper.getByPostId(postId);
        if (post == null) {
            throw new PostNotFoundException(MessageConstant.POST_NOT_FOUND);
        }

        if (post.getIsDeleted() == 0) {
            return buildPostAdminVO(post);
        }

        // 恢复帖子并清除删除时间
        Post postToUpdate = Post.builder()
                .postId(postId)
                .isDeleted(0)
                .deleteTime(null)
                .updateTime(LocalDateTime.now())
                .build();

        postAdminMapper.update(postToUpdate);

        Post updatedPost = postAdminMapper.getByPostId(postId);
        return buildPostAdminVO(updatedPost);
    }

    /**
     * 手动物理删除帖子 - 使用逻辑外键+事务控制
     */
    @Override
    @Transactional
    public void permanentlyDeletePost(Long postId) {
        Post post = postAdminMapper.getByPostId(postId);
        if (post == null) {
            throw new PostNotFoundException(MessageConstant.POST_NOT_FOUND);
        }

        try {
            // 1. 先查询帖子的所有评论ID
            List<Long> commentIds = commentAdminMapper.getCommentIdsByPostId(postId);
            log.info("开始物理删除帖子及相关数据: postId={}, 评论数量={}", postId, commentIds.size());

            if (!commentIds.isEmpty()) {
                // 2. 先删除评论图片
                commentAdminMapper.batchDeleteCommentImages(commentIds);
                log.info("删除评论图片完成: commentCount={}", commentIds.size());

                // 3. 再删除评论
                commentAdminMapper.batchDeleteComments(commentIds);
                log.info("删除评论完成: commentCount={}", commentIds.size());
            }

            // 4. 删除帖子相关数据
            deletePostRelatedData(postId);

            // 5. 最后删除帖子
            postAdminMapper.delete(postId);
            log.info("物理删除帖子完成: postId={}", postId);

        } catch (Exception e) {
            log.error("物理删除帖子失败: postId={}", postId, e);
            throw new RuntimeException("删除帖子失败: " + e.getMessage());
        }
    }

    /**
     * 删除帖子相关数据
     */
    private void deletePostRelatedData(Long postId) {
        // 删除帖子图片
        postAdminMapper.deletePostImagesByPostId(postId);
        log.info("删除帖子图片: postId={}", postId);

        // 删除帖子标签关联
        postAdminMapper.deletePostTagsByPostId(postId);
        log.info("删除帖子标签关联: postId={}", postId);

        // 删除收藏记录
        postAdminMapper.deleteCollectionsByPostId(postId);
        log.info("删除收藏记录: postId={}", postId);

        // 删除点赞记录
        postAdminMapper.deleteLikesByPostId(postId);
        log.info("删除点赞记录: postId={}", postId);
    }

    /**
     * 自动清理过期帖子 - 使用逻辑外键+事务控制
     */
    @Override
    @Transactional
    public int autoCleanupExpiredPosts() {
        if (!autoCleanupEnabled) {
            log.info("帖子自动清理功能已禁用");
            return 0;
        }

        String lockKey = "lock:job:post:cleanup";
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;
        try {
            // 尝试获取锁，不等待，锁持有时间30分钟（与定时任务一致）
            isLocked = lock.tryLock(0, 30, TimeUnit.MINUTES);
            if (!isLocked) {
                log.info("未获取到分布式锁，帖子清理任务可能正在由其他实例或手动触发执行");
                return 0; // 返回0表示未执行清理
            }

            log.info("成功获取分布式锁，开始执行帖子自动清理任务...");
            return doAutoCleanupExpiredPosts();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("帖子清理任务获取分布式锁时被中断", e);
            throw new RuntimeException("清理任务被中断", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("帖子清理任务分布式锁已释放");
            }
        }
    }

    /**
     * 实际执行清理逻辑
     */
    private int doAutoCleanupExpiredPosts() {
        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(autoCleanupDays);
        List<Post> postsToCleanup = postAdminMapper.getPostsToCleanup(thresholdTime);

        if (postsToCleanup.isEmpty()) {
            log.info("没有找到需要清理的过期帖子");
            return 0;
        }

        List<Long> postIds = postsToCleanup.stream()
                .map(Post::getPostId)
                .collect(Collectors.toList());

        log.info("开始自动清理过期帖子: 数量={}", postIds.size());

        try {
            int totalDeleted = 0;

            // 逐条处理，避免大数据量事务过长
            for (Long postId : postIds) {
                try {
                    // 对每个帖子单独处理，保证事务粒度
                    deleteSinglePostWithRelatedData(postId);
                    totalDeleted++;
                    log.info("自动清理帖子成功: postId={}", postId);
                } catch (Exception e) {
                    log.error("自动清理帖子失败: postId={}", postId, e);
                    // 继续处理其他帖子，不因单个失败影响整体
                }
            }

            log.info("自动清理任务完成: 成功清理{}个帖子", totalDeleted);
            return totalDeleted;

        } catch (Exception e) {
            log.error("自动清理任务执行失败", e);
            throw new RuntimeException("自动清理任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取自动清理配置
     */
    @Override
    public PostAutoCleanupConfig getAutoCleanupConfig() {
        // 计算下次清理时间（明天凌晨2点）
        LocalDateTime nextCleanup = LocalDateTime.now()
                .plusDays(1)
                .withHour(2)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        return PostAutoCleanupConfig.builder()
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
     * 删除单个帖子及相关数据
     */
    private void deleteSinglePostWithRelatedData(Long postId) {
        // 1. 查询帖子的所有评论ID
        List<Long> commentIds = commentAdminMapper.getCommentIdsByPostId(postId);

        if (!commentIds.isEmpty()) {
            // 2. 删除评论图片
            commentAdminMapper.batchDeleteCommentImages(commentIds);

            // 3. 删除评论
            commentAdminMapper.batchDeleteComments(commentIds);
        }

        // 4. 删除帖子相关数据
        deletePostRelatedData(postId);

        // 5. 删除帖子
        postAdminMapper.delete(postId);
    }


    /**
     * 构建PostAdminVO对象
     */
    private PostAdminVO buildPostAdminVO(Post post) {
        // 查询用户信息
        String username = "用户";
        String avatarUrl = "/default-avatar.png";
        try {
            User user = accountMapper.getByUserId(post.getUserId());
            if (user != null) {
                username = user.getUsername() != null ? user.getUsername() : "用户" + post.getUserId();
                avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : "/default-avatar.png";
            } else {
                username = "用户" + post.getUserId();
            }
        } catch (Exception e) {
            username = "用户" + post.getUserId();
        }

        // 查询评论数量
        Integer commentCount = 0;
        try {
            commentCount = postAdminMapper.getCommentCount(post.getPostId());
        } catch (Exception e) {
            // 忽略异常
        }

        // 构建管理专用的VO，包含所有管理字段
        return PostAdminVO.builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .username(username)
                .avatarUrl(avatarUrl)
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0)
                .commentCount(commentCount)
                .viewCount(post.getViewCount() != null ? post.getViewCount() : 0)
                .createTime(post.getCreateTime())
                .deleteTime(post.getDeleteTime()) // 删除时间
                .isDeleted(post.getIsDeleted())   // 删除状态
                .updateTime(post.getUpdateTime()) // 更新时间
                .liked(false) // 管理员接口不关注点赞状态
                .build();
    }
}