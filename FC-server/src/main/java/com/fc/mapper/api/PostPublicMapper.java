package com.fc.mapper.api;

import com.fc.entity.Post;
import com.fc.vo.post.PostSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}