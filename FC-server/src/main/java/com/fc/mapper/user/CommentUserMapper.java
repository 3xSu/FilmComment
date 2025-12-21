package com.fc.mapper.user;

import com.fc.entity.Comment;
import com.fc.entity.CommentImages;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface CommentUserMapper {

    /**
     * 插入评论
     */
    @Insert("insert into comment(user_id, post_id, parent_id, content, like_count, is_deleted, create_time, update_time) " +
            "values(#{userId}, #{postId}, #{parentId}, #{content}, #{likeCount}, #{isDeleted}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "commentId")
    void insert(Comment comment);

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
     * 逻辑删除评论
     */
    @Update("update comment set is_deleted = 1, delete_time = #{deleteTime}, update_time = #{updateTime} where comment_id = #{commentId} and user_id = #{userId}")
    int deleteComment(@Param("commentId") Long commentId, @Param("userId") Long userId, @Param("deleteTime") LocalDateTime deleteTime, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 插入评论图片
     */
    @Insert("insert into comment_images(comment_id, image_url, sort_order, create_time) " +
            "values(#{commentId}, #{imageUrl}, #{sortOrder}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCommentImage(CommentImages commentImage);

    /**
     * 统计帖子的评论数量（包括回复，排除已删除的）
     */
    @Select("SELECT COUNT(*) FROM comment WHERE post_id = #{postId} AND is_deleted = 0")
    Integer countCommentsByPostId(@Param("postId") Long postId);
}