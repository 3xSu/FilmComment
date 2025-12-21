package com.fc.mapper.admin;

import com.fc.annotation.AutoFill;
import com.fc.entity.Comment;
import com.fc.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentAdminMapper {

    /**
     * 根据评论ID查询评论（包含已删除的评论）
     */
    @Select("select * from comment where comment_id = #{commentId}")
    Comment getByCommentId(Long commentId);

    /**
     * 更新评论信息
     */
    @Update("update comment set is_deleted = #{isDeleted}, delete_time = #{deleteTime}, update_time = #{updateTime} where comment_id = #{commentId}")
    @AutoFill(OperationType.UPDATE)
    void update(Comment comment);

    /**
     * 物理删除评论
     */
    @Delete("delete from comment where comment_id = #{commentId}")
    void delete(Long commentId);

    /**
     * 查询评论的回复数量（包含未删除的回复）
     */
    @Select("select count(*) from comment where parent_id = #{commentId} and is_deleted = 0")
    Integer getReplyCount(Long commentId);

    /**
     * 批量逻辑删除帖子的所有评论
     */
    @Update("update comment set is_deleted = 1, delete_time = now(), update_time = now() where post_id = #{postId} and is_deleted = 0")
    void batchDeleteByPostId(Long postId);

    /**
     * 查询需要自动清理的评论（逻辑删除超过指定天数）
     */
    @Select("select * from comment where is_deleted = 1 and delete_time < #{thresholdTime}")
    List<Comment> getCommentsToCleanup(LocalDateTime thresholdTime);

    /**
     * 批量物理删除评论
     */
    @Delete({
            "<script>",
            "delete from comment where comment_id in ",
            "<foreach collection='commentIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void batchDeleteComments(@Param("commentIds") List<Long> commentIds);

    /**
     * 根据帖子ID查询所有评论ID
     */
    @Select("select comment_id from comment where post_id = #{postId}")
    List<Long> getCommentIdsByPostId(Long postId);

    /**
     * 批量删除评论图片
     */
    @Delete({
            "<script>",
            "delete from comment_images where comment_id in ",
            "<foreach collection='commentIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void batchDeleteCommentImages(@Param("commentIds") List<Long> commentIds);

    /**
     * 根据评论ID删除评论图片
     */
    @Delete("delete from comment_images where comment_id = #{commentId}")
    void deleteCommentImagesByCommentId(Long commentId);

}