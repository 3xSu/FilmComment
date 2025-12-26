package com.fc.service.api;

import com.fc.dto.post.PostPageQueryDTO;
import com.fc.result.PageResult;
import com.fc.vo.post.PostVO;

public interface PostPublicService {

    /**
     * 分页查询帖子列表（滚动分页）
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    PageResult pageQueryPosts(PostPageQueryDTO pageQueryDTO);

    /**
     * 根据ID获取帖子详情
     * @param postId 帖子ID
     * @return 帖子详情
     */
    PostVO getPostById(Long postId);

    /**
     * 根据用户ID分页查询帖子
     * @param userId 用户ID
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    PageResult pageQueryPostsByUserId(Long userId, PostPageQueryDTO pageQueryDTO);

    /**
     * 统计用户发布的帖子数量
     * @param userId 用户ID
     * @return 帖子数量
     */
    int countUserPosts(Long userId);

    /**
     * 根据电影ID查询所有帖子总数量
     * @param movieId
     * @return
     */
    Integer countPostsByMovieId(Long movieId);
}