package com.fc.service.api;

import com.fc.dto.tag.TagPageQueryDTO;
import com.fc.entity.CreativeTag;
import com.fc.result.PageResult;
import com.fc.vo.tag.TagVO;

import java.util.List;

public interface TagPublicService {

    /**
     * 分页查询标签（适合无限滚动流）
     */
    PageResult pageQueryTags(TagPageQueryDTO pageQueryDTO);

    /**
     * 根据名称前缀搜索标签（支持分页）
     */
    PageResult searchTagsByPrefix(TagPageQueryDTO pageQueryDTO);

    /**
     * 根据名称搜索标签（兼容旧版本，返回列表）
     */
    List<CreativeTag> searchTagsByName(String keyword);

    /**
     * 创建标签（如果不存在）
     */
    CreativeTag createTagIfNotExists(String tagName);

    /**
     * 获取热门标签
     */
    List<TagVO> getHotTags(Integer limit);

    /**
     * 根据ID获取标签详情
     */
    CreativeTag getTagById(Long tagId);
}