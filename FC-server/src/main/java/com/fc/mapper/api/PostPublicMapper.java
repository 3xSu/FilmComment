package com.fc.mapper.api;

import com.fc.entity.Post;
import com.fc.vo.post.PostSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PostPublicMapper {

    /**
     * 根据ID查询帖子（包含已删除的帖子，用于管理员）
     */
    Post getByPostId(Long postId);

    /**
     * 根据ID查询未删除的帖子
     */
    Post getByPostIdNotDeleted(Long postId);

    /**
     * 游标分页查询帖子（按创建时间倒序）
     */
    List<PostSearchVO> pageQueryPostsByCursor(@Param("cursor") LocalDateTime cursor,
                                              @Param("size") Integer size,
                                              @Param("movieId") Long movieId,
                                              @Param("postType") Integer postType,
                                              @Param("contentForm") Integer contentForm,
                                              @Param("spoilerType") Integer spoilerType);

    /**
     * 统计帖子数量（用于分页）
     */
    long countPosts(@Param("movieId") Long movieId,
                    @Param("postType") Integer postType,
                    @Param("contentForm") Integer contentForm,
                    @Param("spoilerType") Integer spoilerType);

    /**
     * 根据用户ID查询帖子列表
     * @param cursor 游标（时间戳）
     * @param size 每页大小
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<PostSearchVO> pageQueryPostsByUserId(
            @Param("cursor") LocalDateTime cursor,
            @Param("size") int size,
            @Param("userId") Long userId);

    /**
     * 统计用户发布的帖子总数
     * @param userId 用户ID
     * @return 帖子总数
     */
    @Select("select count(*) from post where user_id = #{userId} and is_deleted = 0")
    int countPostsByUserId(@Param("userId") Long userId);
}