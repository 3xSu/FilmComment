package com.fc.mapper.user;

import com.fc.entity.*;
import com.fc.vo.tag.TagVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PostUserMapper {

    /**
     * 插入帖子
     */
    @Insert("insert into post(user_id, movie_id, title, content, post_type, content_form, video_url, view_count, like_count, collect_count, is_deleted, create_time, update_time) " +
            "values(#{userId}, #{movieId}, #{title}, #{content}, #{postType}, #{contentForm}, #{videoUrl}, #{viewCount}, #{likeCount}, #{collectCount}, #{isDeleted}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "postId")
    void insert(Post post);

    /**
     * 插入帖子标签关联
     */
    void insertPostTags(@Param("postTags") List<PostTag> postTags);

    /**
     * 插入帖子图片（单张）
     */
    @Insert("insert into post_images(post_id, image_url, sort_order, create_time) " +
            "values(#{postId}, #{imageUrl}, #{sortOrder}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "imageId")
    void insertPostImage(PostImages postImage);

    /**
     * 插入帖子图片
     */
    void insertPostImages(@Param("postImages") List<PostImages> postImages);

    /**
     * 根据ID查询帖子
     */
    @Select("select * from post where post_id = #{postId} and is_deleted = 0")
    Post getByPostId(Long postId);

    /**
     * 根据帖子ID查询Tag
     * @param postId
     * @return
     */
    List<TagVO> getTagsByPostId(Long postId);

    /**
     * 根据帖子ID查询电影信息
     */
    @Select("SELECT m.* FROM movie m INNER JOIN post p ON m.movie_id = p.movie_id WHERE p.post_id = #{postId}")
    Movie getMovieByPostId(Long postId);

    /**
     * 根据帖子ID查询图片URL列表
     */
    @Select("SELECT image_url FROM post_images WHERE post_id = #{postId} ORDER BY sort_order")
    List<String> getImagesByPostId(Long postId);

    /**
     * 插入帖子点赞关系
     */
    void insertPostLike(PostLike postLike);

    /**
     * 删除帖子点赞关系
     */
    @Delete("delete from post_like where user_id = #{userId} and post_id = #{postId}")
    void deletePostLike(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 根据用户ID和帖子ID查询点赞关系
     */
    @Select("select * from post_like where user_id = #{userId} and post_id = #{postId}")
    PostLike getPostLikeByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 更新帖子点赞数
     */
    @Update("update post set like_count = like_count + #{increment}, update_time = now() where post_id = #{postId}")
    void updatePostLikeCount(@Param("postId") Long postId, @Param("increment") Integer increment);

    /**
     * 根据帖子ID查询点赞数量
     */
    @Select("select like_count from post where post_id = #{postId}")
    Integer getPostLikeCount(Long postId);

    /**
     * 根据点赞ID查询点赞记录
     */
    @Select("select * from post_like where like_id = #{likeId}")
    PostLike getPostLikeById(@Param("likeId") Long likeId);

    /**
     * 根据帖子ID查询帖子作者ID
     */
    @Select("select user_id from post where post_id = #{postId} and is_deleted = 0")
    Long getPostAuthorId(@Param("postId") Long postId);

    /**
     * 插入帖子收藏关系
     */
    @Insert("insert into collection(user_id, post_id, create_time) values(#{userId}, #{postId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "collectionId")
    void insertCollection(PostCollection postCollection);

    /**
     * 删除帖子收藏关系
     */
    @Delete("delete from collection where user_id = #{userId} and post_id = #{postId}")
    void deleteCollection(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 根据用户ID和帖子ID查询收藏关系
     */
    @Select("select * from collection where user_id = #{userId} and post_id = #{postId}")
    PostCollection getCollectionByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 更新帖子收藏数
     */
    @Update("update post set collect_count = collect_count + #{increment}, update_time = now() where post_id = #{postId}")
    void updatePostCollectCount(@Param("postId") Long postId, @Param("increment") Integer increment);

    /**
     * 根据帖子ID查询收藏数量
     */
    @Select("select collect_count from post where post_id = #{postId}")
    Integer getPostCollectCount(Long postId);


    /**
     * 游标分页查询用户收藏的帖子
     */
    List<Post> getCollectionsByCursor(@Param("userId") Long userId,
                                      @Param("cursor") LocalDateTime cursor,
                                      @Param("size") Integer size);
    /**
     * 统计用户收藏的帖子数量
     */
    @Select("select count(*) from collection c left join post p on c.post_id = p.post_id where c.user_id = #{userId} and p.is_deleted = 0")
    long countUserCollections(Long userId);

    /**
     * 逻辑删除帖子
     */
    @Update("update post set is_deleted = 1, update_time = now() where post_id = #{postId}")
    void logicalDeletePost(Long postId);

    /**
     * 更新帖子视频URL
     */
    @Update("update post set video_url = #{videoUrl}, update_time = now() where post_id = #{postId}")
    void updatePostVideoUrl(@Param("postId") Long postId, @Param("videoUrl") String videoUrl);

    /**
     * 统计电影的有效帖子数量
     */
    @Select("SELECT COUNT(*) FROM post WHERE movie_id = #{movieId} AND is_deleted = 0")
    Integer countValidPostsByMovieId(Long movieId);


    /**
     * 获取电影的评论样本（按帖子类型和点赞数、时间排序）
     */
    @Select("SELECT p.content FROM post p " +
            "WHERE p.movie_id = #{movieId} AND p.post_type = #{postType} AND p.is_deleted = 0 " +
            "ORDER BY p.like_count DESC, p.create_time DESC LIMIT #{sampleSize}")
    List<String> getCommentSamplesByPostType(@Param("movieId") Long movieId,
                                             @Param("sampleSize") Integer sampleSize,
                                             @Param("postType") Integer postType);

    /**
     * 根据电影ID和帖子类型统计有效帖子数量
     * @param movieId
     * @param postType
     * @return
     */
    @Select("SELECT COUNT(*) FROM post WHERE movie_id = #{movieId} AND post_type = #{postType} AND is_deleted = 0")
    Integer countValidPostsByMovieIdAndPostType(@Param("movieId") Long movieId, @Param("postType") Integer postType);
}