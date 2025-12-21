package com.fc.mapper.user;

import com.fc.vo.post.PostStatVO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PostStatMapper {

    /**
     * 增加帖子浏览量
     */
    @Update("UPDATE post SET view_count = view_count + 1, update_time = NOW() WHERE post_id = #{postId}")
    void incrementViewCount(@Param("postId") Long postId);

    /**
     * 更新帖子点赞数
     */
    @Update("UPDATE post SET like_count = #{likeCount}, update_time = NOW() WHERE post_id = #{postId}")
    void updateLikeCount(@Param("postId") Long postId, @Param("likeCount") Integer likeCount);

    /**
     * 更新帖子评论数
     */
    @Update("UPDATE post SET comment_count = #{commentCount}, update_time = NOW() WHERE post_id = #{postId}")
    void updateCommentCount(@Param("postId") Long postId, @Param("commentCount") Integer commentCount);

    /**
     * 查询帖子统计信息
     */
    @Select("SELECT post_id, like_count, comment_count, view_count, UNIX_TIMESTAMP(update_time) * 1000 as lastUpdateTime " +
            "FROM post WHERE post_id = #{postId} AND is_deleted = 0")
    PostStatVO selectPostStats(@Param("postId") Long postId);
}