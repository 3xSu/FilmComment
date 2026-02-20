package com.fc.mapper.api;

import com.fc.entity.CreativeTag;
import com.fc.vo.tag.TagVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TagPublicMapper {

    /**
     * 游标分页查询标签（按创建时间倒序）
     */
    List<CreativeTag> pageQueryTagsByCursor(@Param("cursor") LocalDateTime cursor,
                                            @Param("size") Integer size,
                                            @Param("keyword") String keyword);

    /**
     * 前缀匹配搜索标签（按标签名称前缀匹配）
     */
    List<CreativeTag> searchTagsByPrefix(@Param("cursor") LocalDateTime cursor,
                                         @Param("size") Integer size,
                                         @Param("keyword") String keyword);

    /**
     * 统计前缀匹配的标签数量
     */
    long countTagsByPrefix(@Param("keyword") String keyword);

    /**
     * 统计标签数量
     */
    long countTags(@Param("keyword") String keyword);

    /**
     * 根据标签名称查询标签
     */
    @Select("SELECT tag_id, tag_name, create_time, update_time, hot_score, usage_count FROM creative_tag WHERE tag_name = #{tagName}")
    CreativeTag getByTagName(String tagName);

    /**
     * 插入标签
     */
    @Insert("INSERT INTO creative_tag (tag_name, create_time, update_time) " +
            "VALUES (#{tagName}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "tagId")
    void insert(CreativeTag creativeTag);

    /**
     * 根据ID查询标签
     */
    @Select("SELECT tag_id, tag_name, create_time, update_time, hot_score, usage_count FROM creative_tag WHERE tag_id = #{tagId}")
    CreativeTag getByTagId(Long tagId);

    /**
     * 查询热门标签（按使用次数排序）
     */
    List<TagVO> getHotTags(@Param("limit") Integer limit);

    /**
     * 统计标签总使用次数
     */
    @Select("SELECT COUNT(*) FROM post_tag WHERE tag_id = #{tagId}")
    Integer countTagTotalUsage(@Param("tagId") Long tagId);

    /**
     * 统计标签近期使用次数
     */
    @Select("SELECT COUNT(*) FROM post_tag WHERE tag_id = #{tagId} AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    Integer countTagRecentUsage(@Param("tagId") Long tagId, @Param("days") Integer days);

    /**
     * 统计标签关联的帖子数量（去重）
     */
    @Select("SELECT COUNT(DISTINCT post_id) FROM post_tag WHERE tag_id = #{tagId}")
    Integer countTagRelatedPosts(@Param("tagId") Long tagId);

    /**
     * 查询所有标签ID
     * 用于初始化布隆过滤器
     * @return 所有标签ID列表
     */
    @Select("SELECT tag_id FROM creative_tag")
    List<Long> selectAllTagIds();
}