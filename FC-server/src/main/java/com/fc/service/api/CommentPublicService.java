package com.fc.service.api;

import com.fc.dto.comment.CommentPageQueryDTO;
import com.fc.result.PageResult;
import com.fc.vo.comment.CommentVO;

import java.util.List;

public interface CommentPublicService {

    /**
     * 分页查询评论列表（滚动分页）
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    PageResult getCommentList(CommentPageQueryDTO pageQueryDTO);

    /**
     * 查询评论的回复列表
     * @param commentId
     * @return
     */
    List<CommentVO> getCommentReplies(Long commentId);

    /**
     * 根据帖子ID统计评论数量
     * @param postId 帖子ID
     * @param includeReplies 是否包含回复（true-包含所有评论和回复，false-只包含顶级评论）
     * @return 评论数量
     */
    Integer countCommentsByPostId(Long postId, boolean includeReplies);
}
