package com.fc.service.impl.api;

import com.fc.constant.MessageConstant;
import com.fc.context.BaseContext;
import com.fc.dto.post.PostPageQueryDTO;
import com.fc.entity.Post;
import com.fc.entity.User;
import com.fc.enums.PostTypeEnum;
import com.fc.exception.AccessDeniedException;
import com.fc.exception.PostNotFoundException;
import com.fc.exception.UserNotFoundException;
import com.fc.mapper.api.AccountMapper;
import com.fc.mapper.api.PostPublicMapper;
import com.fc.mapper.user.PostUserMapper;
import com.fc.result.PageResult;
import com.fc.service.api.CommentPublicService;
import com.fc.service.api.PostPublicService;
import com.fc.service.user.MovieUserService;
import com.fc.service.user.PostStatService;
import com.fc.vo.post.PostSearchVO;
import com.fc.vo.post.PostVO;
import com.fc.vo.tag.TagVO;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostPublicServiceImpl implements PostPublicService {

    @Autowired
    private PostPublicMapper postPublicMapper;

    @Autowired
    private PostUserMapper postUserMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private MovieUserService movieUserService;

    @Autowired
    private PostStatService postStatService;

    @Autowired
    private CommentPublicService commentPublicService;

    /**
     * 分页查询帖子列表（滚动分页）
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryPosts(PostPageQueryDTO pageQueryDTO) {
        // 获取分页参数
        int size = pageQueryDTO.getSize() != null ? pageQueryDTO.getSize() : 20;
        Long movieId = pageQueryDTO.getMovieId();
        Integer postType = pageQueryDTO.getPostType();
        Integer contentForm = pageQueryDTO.getContentForm();

        // 如果是查询深度讨论区帖子，检查用户权限
        if (PostTypeEnum.isSpoiler(postType)) {
            if (!checkSpoilerPermission(movieId)) {
                // 权限不足时返回空结果
                return buildEmptyPageResult();
            }
        }

        // 查询帖子列表
        List<PostSearchVO> records = postPublicMapper.pageQueryPostsByCursor(
                pageQueryDTO.getCursor(), size, movieId, postType, contentForm);

        // 为每个帖子设置标签信息
        records = records.stream()
                .map(this::enrichPostWithTags)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setRecords(records);

        // 设置是否有下一页和下一个游标
        if (!records.isEmpty()) {
            PostSearchVO lastRecord = records.get(records.size() - 1);
            pageResult.setNextCursor(lastRecord.getCreateTime());
            pageResult.setHasNext(records.size() == size);
        } else {
            pageResult.setHasNext(false);
        }

        // 对于第一页查询，返回总记录数；后续页不返回
        if (pageQueryDTO.getCursor() == null) {
            long total = postPublicMapper.countPosts(movieId, postType, contentForm);
            pageResult.setTotal(total);
        } else {
            pageResult.setTotal(-1);
        }

        return pageResult;
    }

    /**
     * 检查深度讨论区权限（针对特定电影）
     * @param movieId 电影ID
     */
    private boolean checkSpoilerPermission(Long movieId) {
        try {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                log.warn("用户未登录，无法访问深度讨论区");
                return false;
            }

            // 检查用户角色（管理员不受限制）
            User user = accountMapper.getByUserId(userId);
            if (user != null && user.getRole() == 2) {
                return true;
            }

            // 检查用户是否标记这部电影为"已看过"
            Integer relationType = movieUserService.checkUserMovieRelation(userId, movieId);

            if (relationType == null || relationType != 2) {
                log.warn("用户{}没有权限访问电影{}的深度讨论区", userId, movieId);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("检查深度讨论区权限时发生错误", e);
            throw new AccessDeniedException("权限检查失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取帖子详情
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @Override
    public PostVO getPostById(Long postId) {
        // 查询帖子基本信息
        Post post = postPublicMapper.getByPostIdNotDeleted(postId);
        if (post == null) {
            throw new PostNotFoundException(MessageConstant.POST_NOT_FOUND);
        }

        // 增加帖子浏览量
        postStatService.incrementViewCount(postId);

        // 构建完整的帖子VO
        return buildPostVO(post);
    }

    /**
     * 为帖子丰富标签信息
     * @param postSearchVO 帖子搜索VO
     * @return 丰富后的帖子搜索VO
     */
    private PostSearchVO enrichPostWithTags(PostSearchVO postSearchVO) {
        try {
            // 查询帖子的标签信息
            List<TagVO> tags = postUserMapper.getTagsByPostId(postSearchVO.getPostId());
            if (tags != null && !tags.isEmpty()) {
                // 只取前3个标签展示
                tags = tags.stream().limit(3).collect(Collectors.toList());
                postSearchVO.setTags(tags);
            }
        } catch (Exception e) {
            log.error("获取帖子标签失败: postId={}", postSearchVO.getPostId(), e);
        }
        return postSearchVO;
    }

    /**
     * 根据用户ID分页查询帖子
     * @param userId 用户ID
     * @param pageQueryDTO 分页参数
     * @return
     */
    @Override
    public PageResult pageQueryPostsByUserId(Long userId, PostPageQueryDTO pageQueryDTO) {
        // 验证用户是否存在
        User user = accountMapper.getByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 获取分页参数
        int size = pageQueryDTO.getSize() != null ? pageQueryDTO.getSize() : 20;

        // 查询帖子列表
        List<PostSearchVO> records = postPublicMapper.pageQueryPostsByUserId(
                pageQueryDTO.getCursor(), size, userId);

        // 为每个帖子设置标签信息
        records = records.stream()
                .map(this::enrichPostWithTags)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setRecords(records);

        // 设置是否有下一页和下一个游标
        if (!records.isEmpty()) {
            PostSearchVO lastRecord = records.get(records.size() - 1);
            pageResult.setNextCursor(lastRecord.getCreateTime());
            pageResult.setHasNext(records.size() == size);
        } else {
            pageResult.setHasNext(false);
        }

        // 对于第一页查询，返回总记录数；后续页不返回
        if (pageQueryDTO.getCursor() == null) {
            long total = postPublicMapper.countPostsByUserId(userId);
            pageResult.setTotal(total);
        } else {
            pageResult.setTotal(-1);
        }

        return pageResult;
    }

    /**
     * 统计用户发布的帖子数量
     * @param userId 用户ID
     * @return
     */
    @Override
    public int countUserPosts(Long userId) {
        log.info("统计用户帖子数量: userId={}", userId);
        return postPublicMapper.countPostsByUserId(userId);
    }

    /**
     * 根据电影ID查询所有帖子总数量
     * @param movieId
     * @return
     */
    @Override
    public Integer countPostsByMovieId(Long movieId){
        return postUserMapper.countValidPostsByMovieId(movieId);
    }

    /**
     * 构建完整的PostVO对象
     * @param post 帖子实体
     * @return PostVO
     */
    private PostVO buildPostVO(Post post) {
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

        // 查询评论数量
        Integer commentCount = 0;
        try {
            // 获取帖子的评论数量
            commentCount = commentPublicService.countCommentsByPostId(post.getPostId(), true);
            if (commentCount == null) {
                commentCount = 0;
            }
        } catch (Exception e) {
            log.error("获取帖子评论数量失败: postId={}", post.getPostId(), e);
            commentCount = 0;
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
                .commentCount(commentCount)
                .createTime(post.getCreateTime())
                .imageUrls(imageUrls)
                .tags(tags)
                .liked(false) // 需要后续实现点赞状态检查
                .collected(false) // 需要后续实现收藏状态检查
                .build();
    }

    /**
     * 构建空的分页结果
     * @return
     */
    private PageResult buildEmptyPageResult() {
        PageResult pageResult = new PageResult();
        pageResult.setRecords(Collections.emptyList());
        pageResult.setHasNext(false);
        pageResult.setTotal(0);
        return pageResult;
    }
}