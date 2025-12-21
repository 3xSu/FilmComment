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
}