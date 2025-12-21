package com.fc.mapper.admin;

import com.fc.annotation.AutoFill;
import com.fc.entity.Post;
import com.fc.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PostAdminMapper {

    /**
     * 根据帖子ID查询帖子（包含已删除的帖子）
     */
    @Select("select * from post where post_id = #{postId}")
    Post getByPostId(Long postId);

    /**
     * 更新帖子信息
     */
    @Update("update post set is_deleted = #{isDeleted}, delete_time = #{deleteTime}, update_time = #{updateTime} where post_id = #{postId}")
    @AutoFill(OperationType.UPDATE)
    void update(Post post);

    /**
     * 物理删除帖子
     */
    @Delete("delete from post where post_id = #{postId}")
    void delete(Long postId);

    /**
     * 查询帖子的评论数量（包含未删除的评论）
     */
    @Select("select count(*) from comment where post_id = #{postId} and is_deleted = 0")
    Integer getCommentCount(Long postId);

    /**
     * 查询需要自动清理的帖子（逻辑删除超过指定天数）
     */
    @Select("select * from post where is_deleted = 1 and delete_time < #{thresholdTime}")
    List<Post> getPostsToCleanup(LocalDateTime thresholdTime);

    /**
     * 删除帖子图片
     */
    @Delete("delete from post_images where post_id = #{postId}")
    void deletePostImagesByPostId(Long postId);

    /**
     * 批量删除帖子图片
     */
    @Delete({
            "<script>",
            "delete from post_images where post_id in ",
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void batchDeletePostImages(@Param("postIds") List<Long> postIds);

    /**
     * 删除帖子标签关联
     */
    @Delete("delete from post_tag where post_id = #{postId}")
    void deletePostTagsByPostId(Long postId);

    /**
     * 批量删除帖子标签关联
     */
    @Delete({
            "<script>",
            "delete from post_tag where post_id in ",
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void batchDeletePostTags(@Param("postIds") List<Long> postIds);

    /**
     * 批量物理删除帖子
     */
    @Delete({
            "<script>",
            "delete from post where post_id in ",
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void batchDeletePosts(@Param("postIds") List<Long> postIds);

    /**
     * 删除帖子收藏记录
     */
    @Delete("delete from collection where post_id = #{postId}")
    void deleteCollectionsByPostId(Long postId);

    /**
     * 删除帖子点赞记录
     */
    @Delete("delete from post_like where post_id = #{postId}")
    void deleteLikesByPostId(Long postId);

    /**
     * 批量删除帖子收藏记录
     */
    @Delete({
            "<script>",
            "delete from collection where post_id in ",
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void batchDeleteCollections(@Param("postIds") List<Long> postIds);

    /**
     * 批量删除帖子点赞记录
     */
    @Delete({
            "<script>",
            "delete from post_like where post_id in ",
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void batchDeleteLikes(@Param("postIds") List<Long> postIds);

}