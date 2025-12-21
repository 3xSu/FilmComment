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
     */
    @Select("select * from comment where comment_id = #{commentId} and is_deleted = 0")
    Comment getByCommentId(Long commentId);

    /**
     * 查询评论的回复数量
     */
    @Select("select count(*) from comment where parent_id = #{commentId} and is_deleted = 0")
    Integer getReplyCount(Long commentId);

    /**
     * 游标分页查询评论（按创建时间倒序）
     */
    @Select("select * from comment where post_id = #{postId} and is_deleted = 0 " +
            "and (#{cursor} is null or create_time < #{cursor}) " +
            "order by create_time desc limit #{size}")
    List<Comment> pageQueryCommentsByCursor(@Param("postId") Long postId,
                                            @Param("cursor") LocalDateTime cursor,
                                            @Param("size") Integer size);

    /**
     * 查询顶级评论（parent_id=0）用于分页
     */
    @Select("select * from comment where post_id = #{postId} and parent_id = 0 and is_deleted = 0 " +
            "and (#{cursor} is null or create_time < #{cursor}) " +
            "order by create_time desc limit #{size}")
    List<Comment> pageQueryTopCommentsByCursor(@Param("postId") Long postId,
                                               @Param("cursor") LocalDateTime cursor,
                                               @Param("size") Integer size);

    /**
     * 查询评论的回复列表
     */
    @Select("select * from comment where parent_id = #{parentId} and is_deleted = 0 " +
            "order by create_time asc") // 回复按时间正序排列
    List<Comment> getRepliesByParentId(Long parentId);
}
