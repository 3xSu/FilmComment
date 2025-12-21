package com.fc.service.impl.api;

import com.fc.dto.comment.CommentPageQueryDTO;
import com.fc.entity.Comment;
import com.fc.entity.User;
import com.fc.mapper.api.AccountMapper;
import com.fc.mapper.api.CommentPublicMapper;
import com.fc.result.PageResult;
import com.fc.service.api.CommentPublicService;
import com.fc.vo.comment.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentPublicServiceImpl implements CommentPublicService {

    @Autowired
    private CommentPublicMapper commentPublicMapper;

    @Autowired
    private AccountMapper accountMapper;

    /**
     * 查询评论列表
     * @param pageQueryDTO 分页参数
     * @return
     */
    @Override
    public PageResult getCommentList(CommentPageQueryDTO pageQueryDTO) {
        Long postId = pageQueryDTO.getPostId();
        LocalDateTime cursor = pageQueryDTO.getCursor();
        Integer size = pageQueryDTO.getSize() != null ? pageQueryDTO.getSize() : 20;

        // 查询评论列表（只查询顶级评论，parent_id=0）
        List<Comment> comments = commentPublicMapper.pageQueryTopCommentsByCursor(postId, cursor, size);

        // 转换为VO列表
        List<CommentVO> commentVOs = comments.stream()
                .map(this::buildCommentVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setRecords(commentVOs);

        // 设置是否有下一页和下一个游标
        if (!comments.isEmpty()) {
            Comment lastComment = comments.get(comments.size() - 1);
            pageResult.setNextCursor(lastComment.getCreateTime());
            pageResult.setHasNext(comments.size() == size);
        } else {
            pageResult.setHasNext(false);
        }

        // 对于滚动分页，不需要总记录数
        pageResult.setTotal(-1);

        return pageResult;
    }

    /**
     * 查询评论的回复列表
     * @param commentId
     * @return
     */
    public List<CommentVO> getCommentReplies(Long commentId) {
        List<Comment> replies = commentPublicMapper.getRepliesByParentId(commentId);
        return replies.stream()
                .map(this::buildCommentVO)
                .collect(Collectors.toList());
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
                replyCount = commentPublicMapper.getReplyCount(comment.getCommentId());
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
