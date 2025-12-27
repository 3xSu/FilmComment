package com.fc.mapper.api;

import com.fc.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentPublicMapper {

    /**
     * 根据ID查询评论
     * @param commentId 评论
     * @return
     */
    @Select("select * from comment where comment_id = #{commentId} and is_deleted = 0")
    Comment getByCommentId(Long commentId);

    /**
     * 查询评论的回复数量
     * @param commentId 评论
     * @return
     */
    @Select("select count(*) from comment where parent_id = #{commentId} and is_deleted = 0")
    Integer getReplyCount(Long commentId);

    /**
     * 游标分页查询评论（按创建时间倒序）
     * @param postId 帖子
     * @param cursor 游标
     * @param size 分页容量
     * @return
     */
    @Select("select * from comment where post_id = #{postId} and is_deleted = 0 " +
            "and (#{cursor} is null or create_time < #{cursor}) " +
            "order by create_time desc limit #{size}")
    List<Comment> pageQueryCommentsByCursor(@Param("postId") Long postId,
                                            @Param("cursor") LocalDateTime cursor,
                                            @Param("size") Integer size);

    /**
     * 查询顶级评论（parent_id=0）用于分页
     * @param postId 顶级评论
     * @param cursor 游标
     * @param size 分页容量大小
     * @return
     */
    @Select("select * from comment where post_id = #{postId} and parent_id = 0 and is_deleted = 0 " +
            "and (#{cursor} is null or create_time < #{cursor}) " +
            "order by create_time desc limit #{size}")
    List<Comment> pageQueryTopCommentsByCursor(@Param("postId") Long postId,
                                               @Param("cursor") LocalDateTime cursor,
                                               @Param("size") Integer size);

    /**
     * 查询评论的回复列表
     * @param parentId 父评论
     * @return
     */
    @Select("select * from comment where parent_id = #{parentId} and is_deleted = 0 " +
            "order by create_time asc") // 回复按时间正序排列
    List<Comment> getRepliesByParentId(Long parentId);

    /**
     * 根据帖子ID统计评论数量（包括所有回复）
     * @param postId 帖子
     * @return
     */
    @Select("select count(*) from comment where post_id = #{postId} and is_deleted = 0")
    Integer countCommentsByPostId(@Param("postId") Long postId);

    /**
     * 根据帖子ID统计顶级评论数量（不包含回复）
     * @param postId 帖子
     * @return
     */
    @Select("select count(*) from comment where post_id = #{postId} and parent_id = 0 and is_deleted = 0")
    Integer countTopCommentsByPostId(@Param("postId") Long postId);
}
