package com.fc.service.impl.user;

import com.fc.context.BaseContext;
import com.fc.dto.post.CollectionPageQueryDTO;
import com.fc.dto.post.PostPublishDTO;
import com.fc.entity.*;
import com.fc.exception.PostNotFoundException;
import com.fc.exception.UnauthorizedException;
import com.fc.mapper.admin.MovieAdminMapper;
import com.fc.mapper.api.AccountMapper;
import com.fc.mapper.user.PostUserMapper;
import com.fc.result.PageResult;
import com.fc.service.api.TagPublicService;
import com.fc.service.user.MovieUserService;
import com.fc.service.user.NotificationService;
import com.fc.service.user.PostStatService;
import com.fc.service.user.PostUserService;
import com.fc.utils.FileSecurityValidator;
import com.fc.vo.post.*;
import com.fc.vo.tag.TagVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostUserServiceImpl implements PostUserService {

    @Autowired
    private PostUserMapper postUserMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private MovieAdminMapper movieAdminMapper;

    @Autowired
    private com.fc.utils.AliOssUtil aliOssUtil;

    @Autowired
    private MovieUserService movieUserService;

    @Autowired
    private TagPublicService tagPublicService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PostStatService postStatService;

    /**
     * 发布帖子
     * @param postPublishDTO 帖子发布信息
     * @return 帖子信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // 明确指定回滚所有异常
    public PostVO publishPost(PostPublishDTO postPublishDTO) {
        log.info("开始发布帖子事务: {}", postPublishDTO);

        Long userId = BaseContext.getCurrentId();
        Long movieId = postPublishDTO.getMovieId();
        LocalDateTime now = LocalDateTime.now();

        // 用于事务回滚时的资源清理
        Post post = null;

        try {
            // 1. 前置验证和权限检查
            // 根据帖子类型判断是否为深度讨论区帖子，检查权限
            if (postPublishDTO.isSpoilerArea()) {
                checkSpoilerPermission(userId, movieId);
            }

            User user = accountMapper.getByUserId(userId);
            Movie movie = movieAdminMapper.getByMovieId(postPublishDTO.getMovieId());
            if (movie == null) {
                throw new RuntimeException("电影不存在");
            }

            // 2. 处理标签逻辑（在事务内进行，任何异常都会回滚）
            List<Long> finalTagIds = processTagsSafely(postPublishDTO);

            // 3. 创建帖子实体
            post = Post.builder()
                    .userId(userId)
                    .movieId(postPublishDTO.getMovieId())
                    .title(postPublishDTO.getTitle())
                    .content(postPublishDTO.getContent())
                    .postType(postPublishDTO.getPostType())
                    .contentForm(postPublishDTO.getContentForm())
                    .videoUrl(postPublishDTO.getVideoUrl())
                    .viewCount(0)
                    .likeCount(0)
                    .collectCount(0)
                    .isDeleted(0)
                    .createTime(now)
                    .updateTime(now)
                    .build();

            // 4. 插入帖子
            postUserMapper.insert(post);
            log.info("帖子插入成功: postId={}", post.getPostId());

            // 5. 处理标签关联
            if (finalTagIds != null && !finalTagIds.isEmpty()) {
                final Long currentPostId = post.getPostId();
                List<PostTag> postTags = finalTagIds.stream()
                        .map(tagId -> PostTag.builder()
                                .postId(currentPostId)
                                .tagId(tagId)
                                .createTime(now)
                                .build())
                        .collect(Collectors.toList());
                postUserMapper.insertPostTags(postTags);
                log.info("标签关联插入成功: 数量={}", postTags.size());
            }

            // 6. 处理图片
            if (postPublishDTO.getImageUrls() != null && !postPublishDTO.getImageUrls().isEmpty()) {
                List<PostImages> postImages = new ArrayList<>();
                for (int i = 0; i < postPublishDTO.getImageUrls().size(); i++) {
                    postImages.add(PostImages.builder()
                            .postId(post.getPostId())
                            .imageUrl(postPublishDTO.getImageUrls().get(i))
                            .sortOrder(i + 1)
                            .createTime(now)
                            .build());
                }
                postUserMapper.insertPostImages(postImages);
                log.info("图片插入成功: 数量={}", postImages.size());
            }

            // 7. 构建返回结果
            PostVO postVO = buildPostVO(post, user, movie);
            log.info("帖子发布事务完成: postId={}", post.getPostId());

            return postVO;

        } catch (Exception e) {
            // 事务会自动回滚，但添加详细的日志记录和资源清理
            log.error("发布帖子事务失败，已回滚。用户ID: {}, 电影ID: {}, 错误信息: {}",
                    userId, movieId, e.getMessage(), e);

            // 发送发布失败通知
            sendPostPublishFailedNotification(userId, e.getMessage());

            // 清理可能产生的临时资源
            cleanupPostResources(post, postPublishDTO);

            // 重新抛出异常，确保事务回滚
            throw new RuntimeException("发布帖子失败: " + e.getMessage(), e);
        }
    }

    /**
     * 安全处理标签（在事务内，任何异常都会导致回滚）
     */
    private List<Long> processTagsSafely(PostPublishDTO postPublishDTO) {
        List<Long> finalTagIds = new ArrayList<>();

        try {
            // 添加现有标签ID
            if (postPublishDTO.getTagIds() != null && !postPublishDTO.getTagIds().isEmpty()) {
                for (Long tagId : postPublishDTO.getTagIds()) {
                    CreativeTag tag = tagPublicService.getTagById(tagId);
                    if (tag == null) {
                        // 改为抛出异常，而不是记录日志继续执行
                        throw new IllegalArgumentException("标签不存在: " + tagId);
                    }
                    finalTagIds.add(tagId);
                    log.info("关联现有标签: tagId={}, tagName={}", tagId, tag.getTagName());
                }
            }

            // 创建新标签
            if (postPublishDTO.getNewTagNames() != null && !postPublishDTO.getNewTagNames().isEmpty()) {
                List<String> newTagNames = postPublishDTO.getNewTagNames().stream()
                        .limit(10)
                        .collect(Collectors.toList());

                for (String tagName : newTagNames) {
                    if (tagName != null && !tagName.trim().isEmpty()) {
                        try {
                            CreativeTag newTag = tagPublicService.createTagIfNotExists(tagName.trim());
                            finalTagIds.add(newTag.getTagId());
                            log.info("创建新标签成功: tagId={}, tagName={}", newTag.getTagId(), newTag.getTagName());
                        } catch (Exception e) {
                            throw new RuntimeException("创建标签失败: " + tagName, e);
                        }
                    }
                }
            }

            return finalTagIds.stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            log.error("标签处理失败，将导致事务回滚", e);
            throw new RuntimeException("标签处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清理帖子相关资源（事务回滚时调用）
     */
    private void cleanupPostResources(Post post, PostPublishDTO postPublishDTO) {
        try {
            // 如果帖子已经创建但事务回滚，可以在这里清理相关资源
            if (post != null && post.getPostId() != null) {
                log.info("清理帖子资源: postId={}", post.getPostId());
                // 这里可以添加清理逻辑
            }

            // 清理已上传的图片文件
            if (postPublishDTO.getImageUrls() != null && !postPublishDTO.getImageUrls().isEmpty()) {
                log.info("开始清理OSS图片文件，数量: {}", postPublishDTO.getImageUrls().size());

                int successCount = aliOssUtil.batchDelete(postPublishDTO.getImageUrls());
                log.info("OSS文件清理完成: 成功删除{}/{}个文件",
                        successCount, postPublishDTO.getImageUrls().size());
            }
        } catch (Exception e) {
            log.error("清理帖子资源失败", e);
            // 不抛出异常，避免掩盖原始错误
        }
    }

    /**
     * 发布帖子失败时发送系统通知
     */
    private void sendPostPublishFailedNotification(Long userId, String errorMessage) {
        try {
            notificationService.sendSystemNotification(
                    userId,
                    "帖子发布失败",
                    "您的帖子发布失败，原因：" + errorMessage,
                    null,
                    0
            );
        } catch (Exception e) {
            log.error("发送帖子发布失败通知异常", e);
        }
    }

    /**
     * 检查深度讨论区权限（针对特定电影）
     * @param userId 用户ID
     * @param movieId 电影ID
     */
    private void checkSpoilerPermission(Long userId, Long movieId) {
        // 检查用户角色（管理员不受限制）
        User user = accountMapper.getByUserId(userId);
        if (user != null && user.getRole() == 2) {
            return;
        }

        // 检查用户是否标记这部电影为"已看过"
        Integer relationType = movieUserService.checkUserMovieRelation(userId, movieId);

        if (relationType == null || relationType != 2) {
            throw new RuntimeException(
                    "需要将这部电影标记为\"已看过\"才能发布深度讨论区帖子");
        }
    }

    /**
     * 点赞帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    @Override
    @Transactional
    public void likePost(Long userId, Long postId) {
        log.info("用户点赞帖子: userId={}, postId={}", userId, postId);

        // 检查帖子是否存在
        Post post = postUserMapper.getByPostId(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new PostNotFoundException("帖子不存在或已被删除");
        }

        // 检查是否已经点赞
        PostLike existingLike = postUserMapper.getPostLikeByUserIdAndPostId(userId, postId);
        if (existingLike != null) {
            log.info("用户已点赞过该帖子: userId={}, postId={}", userId, postId);
            return; // 已经点赞过，直接返回
        }

        // 创建点赞关系
        PostLike postLike = PostLike.builder()
                .userId(userId)
                .postId(postId)
                .createTime(LocalDateTime.now())
                .build();
        postUserMapper.insertPostLike(postLike);

        // 更新帖子点赞数
        postUserMapper.updatePostLikeCount(postId, 1);

        // 获取更新后的点赞数
        Integer latestLikeCount = postUserMapper.getPostLikeCount(postId);

        // 实时更新点赞数
        postStatService.updateLikeCount(postId, latestLikeCount);

        // 发送点赞通知给帖子作者（新增功能）
        sendLikeNotification(post.getUserId(), userId, postId, postLike.getId());

        log.info("帖子点赞成功: userId={}, postId={}", userId, postId);
    }

    /**
     * 发送点赞通知给帖子作者（新增方法）
     */
    private void sendLikeNotification(Long targetUserId, Long likerId, Long postId, Long likeId) {
        try {
            notificationService.sendLikeNotification(targetUserId, likerId, postId, likeId);
            log.debug("点赞通知发送成功: targetUserId={}, likerId={}, postId={}",
                    targetUserId, likerId, postId);
        } catch (Exception e) {
            log.error("发送点赞通知失败: targetUserId={}, likerId={}, postId={}",
                    targetUserId, likerId, postId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 取消点赞帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    @Override
    @Transactional
    public void unlikePost(Long userId, Long postId) {
        log.info("用户取消点赞帖子: userId={}, postId={}", userId, postId);

        // 检查帖子是否存在
        Post post = postUserMapper.getByPostId(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new PostNotFoundException("帖子不存在或已被删除");
        }

        // 检查是否有点赞关系
        PostLike existingLike = postUserMapper.getPostLikeByUserIdAndPostId(userId, postId);
        if (existingLike == null) {
            log.info("用户未点赞过该帖子: userId={}, postId={}", userId, postId);
            return; // 没有点赞关系，直接返回
        }

        // 删除点赞关系
        postUserMapper.deletePostLike(userId, postId);

        // 更新帖子点赞数
        postUserMapper.updatePostLikeCount(postId, -1);

        // 获取更新后的点赞数
        Integer latestLikeCount = postUserMapper.getPostLikeCount(postId);

        // 实时更新点赞数
        postStatService.updateLikeCount(postId, latestLikeCount);

        log.info("帖子取消点赞成功: userId={}, postId={}", userId, postId);
    }

    /**
     * 检查用户是否点赞过帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否点赞
     */
    @Override
    public boolean checkUserLikedPost(Long userId, Long postId) {
        PostLike postLike = postUserMapper.getPostLikeByUserIdAndPostId(userId, postId);
        return postLike != null;
    }

    /**
     * 收藏帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    @Override
    @Transactional
    public void collectPost(Long userId, Long postId) {
        log.info("用户收藏帖子: userId={}, postId={}", userId, postId);

        // 检查帖子是否存在
        Post post = postUserMapper.getByPostId(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new PostNotFoundException("帖子不存在或已被删除");
        }

        // 检查是否已经收藏
        PostCollection existingPostCollection = postUserMapper.getCollectionByUserIdAndPostId(userId, postId);
        if (existingPostCollection != null) {
            log.info("用户已收藏过该帖子: userId={}, postId={}", userId, postId);
            return; // 已经收藏过，直接返回
        }

        // 创建收藏关系
        PostCollection postCollection = PostCollection.builder()
                .userId(userId)
                .postId(postId)
                .createTime(LocalDateTime.now())
                .build();
        postUserMapper.insertCollection(postCollection);

        // 更新帖子收藏数
        postUserMapper.updatePostCollectCount(postId, 1);

        log.info("帖子收藏成功: userId={}, postId={}", userId, postId);
    }

    /**
     * 取消收藏帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    @Override
    @Transactional
    public void uncollectPost(Long userId, Long postId) {
        log.info("用户取消收藏帖子: userId={}, postId={}", userId, postId);

        // 检查帖子是否存在
        Post post = postUserMapper.getByPostId(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new PostNotFoundException("帖子不存在或已被删除");
        }

        // 检查是否有收藏关系
        PostCollection existingPostCollection = postUserMapper.getCollectionByUserIdAndPostId(userId, postId);
        if (existingPostCollection == null) {
            log.info("用户未收藏过该帖子: userId={}, postId={}", userId, postId);
            return; // 没有收藏关系，直接返回
        }

        // 删除收藏关系
        postUserMapper.deleteCollection(userId, postId);

        // 更新帖子收藏数
        postUserMapper.updatePostCollectCount(postId, -1);

        log.info("帖子取消收藏成功: userId={}, postId={}", userId, postId);
    }

    /**
     * 检查用户是否收藏过帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否收藏
     */
    @Override
    public boolean checkUserCollectedPost(Long userId, Long postId) {
        PostCollection postCollection = postUserMapper.getCollectionByUserIdAndPostId(userId, postId);
        return postCollection != null;
    }

    /**
     * 获取用户收藏的帖子列表（分页）
     * @param userId 用户ID
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult getUserCollections(Long userId, CollectionPageQueryDTO pageQueryDTO) {
        log.info("获取用户收藏列表: userId={}, cursor={}, size={}",
                userId, pageQueryDTO.getCursor(), pageQueryDTO.getSize());

        // 获取分页参数
        int size = pageQueryDTO.getSize() != null ? pageQueryDTO.getSize() : 20;
        LocalDateTime cursor = pageQueryDTO.getCursor();

        // 查询收藏的帖子列表
        List<Post> posts = postUserMapper.getCollectionsByCursor(userId, cursor, size);

        // 转换为列表VO
        List<PostListVO> postListVOs = posts.stream()
                .map(this::buildPostListVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setRecords(postListVOs);

        // 设置是否有下一页和下一个游标
        if (!posts.isEmpty()) {
            Post lastPost = posts.get(posts.size() - 1);
            // 使用收藏时间作为游标
            pageResult.setNextCursor(lastPost.getCollectionTime());
            pageResult.setHasNext(posts.size() == size);
        } else {
            pageResult.setHasNext(false);
        }

        // 对于第一页查询，返回总记录数；后续页不返回
        if (cursor == null) {
            long total = postUserMapper.countUserCollections(userId);
            pageResult.setTotal(total);
        } else {
            pageResult.setTotal(-1);
        }

        log.info("用户收藏列表查询成功: userId={}, 返回记录数={}", userId, postListVOs.size());
        return pageResult;
    }

    /**
     * 构建帖子列表VO（用于列表展示）
     * @param post 帖子实体
     * @return PostListVO
     */
    private PostListVO buildPostListVO(Post post) {
        // 从线程局部变量获取当前用户ID
        Long currentUserId = BaseContext.getCurrentId();

        // 查询用户信息
        String username = "用户";
        String avatarUrl = "/default-avatar.png";
        try {
            com.fc.entity.User user = accountMapper.getByUserId(post.getUserId());
            if (user != null) {
                username = user.getUsername() != null ? user.getUsername() : "用户" + post.getUserId();
                avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : "/default-avatar.png";
            } else {
                username = "用户" + post.getUserId();
            }
        } catch (Exception e) {
            username = "用户" + post.getUserId();
        }

        // 查询电影标题
        String movieTitle = "电影";
        try {
            com.fc.entity.Movie movie = postUserMapper.getMovieByPostId(post.getPostId());
            if (movie != null) {
                movieTitle = movie.getTitle() != null ? movie.getTitle() : "电影" + post.getMovieId();
            } else {
                movieTitle = "电影" + post.getMovieId();
            }
        } catch (Exception e) {
            movieTitle = "电影" + post.getMovieId();
        }

        // 查询标签信息
        List<TagVO> tags = null;
        try {
            tags = postUserMapper.getTagsByPostId(post.getPostId());
            // 只显示前3个标签
            if (tags != null && tags.size() > 3) {
                tags = tags.subList(0, 3);
            }
        } catch (Exception e) {
            log.error("获取帖子标签失败: postId={}", post.getPostId(), e);
        }

        // 查询图片列表，获取封面图片
        String coverImage = null;
        try {
            List<String> imageUrls = postUserMapper.getImagesByPostId(post.getPostId());
            if (imageUrls != null && !imageUrls.isEmpty()) {
                coverImage = imageUrls.get(0);
            }
        } catch (Exception e) {
            log.error("获取帖子图片失败: postId={}", post.getPostId(), e);
        }

        // 检查当前用户是否点赞
        boolean liked = false;
        if (currentUserId != null) {
            try {
                liked = checkUserLikedPost(currentUserId, post.getPostId());
            } catch (Exception e) {
                log.error("检查用户点赞状态失败: userId={}, postId={}", currentUserId, post.getPostId(), e);
            }
        }

        // 构建PostListVO（针对列表展示优化）
        return PostListVO.builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .username(username)
                .avatarUrl(avatarUrl)
                .movieId(post.getMovieId())
                .movieTitle(movieTitle)
                .title(post.getTitle())
                .contentSummary(truncateContent(post.getContent()))
                .postType(post.getPostType())
                .contentForm(post.getContentForm())
                .coverImage(coverImage)
                .videoUrl(post.getVideoUrl())
                .viewCount(post.getViewCount() != null ? post.getViewCount() : 0)
                .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0)
                .collectCount(post.getCollectCount() != null ? post.getCollectCount() : 0)
                .commentCount(0) // 需要后续实现评论计数
                .createTime(post.getCreateTime())
                .tags(tags)
                .liked(liked)
                .collected(true) // 收藏列表中的帖子都是已收藏的
                .collectionTime(post.getCollectionTime()) // 收藏时间
                .build();
    }

    /**
     * 构建完整的PostVO对象（用于详情展示）
     * @param post 帖子实体
     * @return PostVO
     */
    private PostVO buildPostVO(Post post, User user, Movie movie) {
        // 1. 查询用户信息
        String username = user != null ?
                (user.getUsername() != null ? user.getUsername() : "用户" + post.getUserId()) :
                "用户" + post.getUserId();
        String avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : "/default-avatar.png";
        Long currentUserId = user.getUserId();

        // 2. 查询电影信息
        String movieTitle = movie != null ?
                (movie.getTitle() != null ? movie.getTitle() : "") :
                "电影" + post.getMovieId();
        Long movieId = movie.getMovieId();

        // 查询标签信息
        List<TagVO> tags = null;
        try {
            tags = postUserMapper.getTagsByPostId(post.getPostId());
        } catch (Exception e) {
            log.error("获取帖子标签失败: postId={}", post.getPostId(), e);
        }

        // 查询图片列表
        List<String> imageUrls = null;
        try {
            imageUrls = postUserMapper.getImagesByPostId(post.getPostId());
        } catch (Exception e) {
            log.error("获取帖子图片失败: postId={}", post.getPostId(), e);
        }

        // 检查当前用户是否点赞和收藏
        boolean liked = false;
        boolean collected = false;
        if (currentUserId != null) {
            try {
                liked = checkUserLikedPost(currentUserId, post.getPostId());
                collected = checkUserCollectedPost(currentUserId, post.getPostId());
            } catch (Exception e) {
                log.error("检查用户互动状态失败: userId={}, postId={}", currentUserId, post.getPostId(), e);
            }
        }

        // 构建完整的PostVO
        return PostVO.builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .username(username)
                .avatarUrl(avatarUrl)
                .movieId(post.getMovieId())
                .movieTitle(movieTitle)
                .title(post.getTitle())
                .content(post.getContent())
                .postType(post.getPostType())
                .contentForm(post.getContentForm())
                .videoUrl(post.getVideoUrl())
                .viewCount(post.getViewCount() != null ? post.getViewCount() : 0)
                .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0)
                .collectCount(post.getCollectCount() != null ? post.getCollectCount() : 0)
                .commentCount(0) // 需要后续实现评论计数
                .createTime(post.getCreateTime())
                .imageUrls(imageUrls)
                .tags(tags)
                .liked(liked)
                .collected(collected)
                .build();
    }

    /**
     * 截断内容，用于列表展示
     * @param content 原始内容
     * @return 截断后的内容
     */
    private String truncateContent(String content) {
        if (content == null) {
            return "";
        }
        if (content.length() <= 100) {
            return content;
        }
        return content.substring(0, 100) + "...";
    }

    /**
     * 删除帖子（逻辑删除）
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        log.info("用户删除帖子: userId={}, postId={}", userId, postId);

        // 检查帖子是否存在
        Post post = postUserMapper.getByPostId(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new PostNotFoundException("帖子不存在或已被删除");
        }

        // 检查用户是否有权限删除（只能删除自己创建的帖子）
        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权删除他人帖子");
        }

        // 执行逻辑删除
        postUserMapper.logicalDeletePost(postId);

        log.info("帖子删除成功: userId={}, postId={}", userId, postId);
    }

    /**
     * 上传帖子图片
     * @param userId 用户ID
     * @param postId 帖子ID
     * @param imageFile 图片文件
     * @param sortOrder 图片排序
     * @return 图片上传信息
     */
    @Override
    @Transactional
    public PostImageUploadVO uploadPostImage(Long userId, Long postId, MultipartFile imageFile, Integer sortOrder) {
        log.info("用户上传帖子图片: userId={}, postId={}, sortOrder={}", userId, postId, sortOrder);

        // 检查帖子是否存在且属于当前用户
        Post post = postUserMapper.getByPostId(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new PostNotFoundException("帖子不存在或已被删除");
        }

        // 检查用户是否有权限操作（只能操作自己创建的帖子）
        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权操作他人帖子");
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

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!isImageFile(extension)) {
            throw new RuntimeException("不支持的文件类型，仅支持jpg、jpeg、png格式");
        }

        // 检查文件大小（限制为5MB）
        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过5MB");
        }

        try {
            //安全文件验证
            FileSecurityValidator.validateImageFile(imageFile);
            // 生成唯一的文件名
            String fileName = generateImageFileName(getSafeExtension(imageFile));

            // 上传到阿里云OSS
            String imageUrl = aliOssUtil.upload(imageFile.getBytes(), fileName);

            // 创建帖子图片实体
            PostImages postImage = PostImages.builder()
                    .postId(postId)
                    .imageUrl(imageUrl)
                    .sortOrder(sortOrder != null ? sortOrder : 1)
                    .createTime(LocalDateTime.now())
                    .build();

            // 插入数据库
            postUserMapper.insertPostImage(postImage);

            // 构建返回结果
            return PostImageUploadVO.builder()
                    .imageId(postImage.getImageId())
                    .postId(postId)
                    .imageUrl(imageUrl)
                    .sortOrder(postImage.getSortOrder())
                    .createTime(postImage.getCreateTime())
                    .build();

        }  catch (IOException e) {
            log.error("帖子图片上传失败: userId={}, postId={}", userId, postId, e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        } catch (SecurityException e) {
            log.warn("文件安全验证失败: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("文件安全验证失败: " + e.getMessage());
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
        return "post-images/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 上传帖子视频
     * @param userId 用户ID
     * @param postId 帖子ID
     * @param videoFile 视频文件
     * @return 视频上传信息
     */
    @Override
    @Transactional
    public PostVideoUploadVO uploadPostVideo(Long userId, Long postId, MultipartFile videoFile) {
        log.info("用户上传帖子视频: userId={}, postId={}", userId, postId);

        // 检查帖子是否存在且属于当前用户
        Post post = postUserMapper.getByPostId(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new PostNotFoundException("帖子不存在或已被删除");
        }

        // 检查用户是否有权限操作（只能操作自己创建的帖子）
        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权操作他人帖子");
        }

        // 检查文件是否为空
        if (videoFile == null || videoFile.isEmpty()) {
            throw new RuntimeException("视频文件不能为空");
        }

        // 检查文件类型
        String originalFilename = videoFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名无效");
        }

        String extension = getSafeVideoExtension(videoFile);
        if (!isVideoFile(extension)) {
            throw new RuntimeException("不支持的文件类型，仅支持mp4、avi、mov格式");
        }

        // 检查文件大小（限制为100MB）
        if (videoFile.getSize() > 100 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过100MB");
        }

        try {
            // 安全文件验证
            FileSecurityValidator.validateVideoFile(videoFile);
            // 生成唯一的文件名
            String fileName = generateVideoFileName(extension);

            // 上传到阿里云OSS
            String videoUrl = aliOssUtil.upload(videoFile.getBytes(), fileName);

            // 更新帖子视频URL
            postUserMapper.updatePostVideoUrl(postId, videoUrl);

            // 构建返回结果
            return PostVideoUploadVO.builder()
                    .postId(postId)
                    .videoUrl(videoUrl)
                    .createTime(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            log.error("帖子视频上传失败: userId={}, postId={}", userId, postId, e);
            throw new RuntimeException("视频上传失败: " + e.getMessage());
        } catch (SecurityException e) {
            log.warn("视频文件安全验证失败: userId={}, postId={}, error={}",
                    userId, postId, e.getMessage());
            throw new RuntimeException("视频文件安全验证失败: " + e.getMessage());
        }
    }

    /**
     * 安全获取视频文件扩展名
     * @param file 视频文件
     * @return 安全的扩展名
     */
    private String getSafeVideoExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return ".mp4"; // 默认扩展名
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 只允许特定的视频扩展名
        List<String> allowedExtensions = Arrays.asList(".mp4", ".avi", ".mov");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            return ".mp4"; // 强制使用安全扩展名
        }

        return extension;
    }

    /**
     * 检查是否为视频文件
     * @param extension 文件扩展名
     * @return 是否为视频文件
     */
    private boolean isVideoFile(String extension) {
        if (extension == null) return false;

        String lowerExtension = extension.toLowerCase();
        return lowerExtension.equals(".mp4") ||
                lowerExtension.equals(".avi") ||
                lowerExtension.equals(".mov");
    }

    /**
     * 生成唯一的视频文件名
     * @param extension 文件扩展名
     * @return 文件名
     */
    private String generateVideoFileName(String extension) {
        return "post-videos/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }
}