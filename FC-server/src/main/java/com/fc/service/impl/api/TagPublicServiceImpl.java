package com.fc.service.impl.api;

import com.fc.dto.tag.TagPageQueryDTO;
import com.fc.entity.CreativeTag;
import com.fc.mapper.api.TagPublicMapper;
import com.fc.result.PageResult;
import com.fc.service.api.TagPublicService;
import com.fc.vo.tag.TagVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TagPublicServiceImpl implements TagPublicService {

    @Autowired
    private TagPublicMapper tagPublicMapper;

    /**
     * 分页查询标签（适合无限滚动流）
     */
    @Override
    public PageResult pageQueryTags(TagPageQueryDTO pageQueryDTO) {
        log.info("分页查询标签: cursor={}, size={}, keyword={}",
                pageQueryDTO.getCursor(), pageQueryDTO.getSize(), pageQueryDTO.getKeyword());

        // 获取分页参数
        int size = pageQueryDTO.getSize() != null ? pageQueryDTO.getSize() : 20;
        LocalDateTime cursor = pageQueryDTO.getCursor();
        String keyword = pageQueryDTO.getKeyword();

        // 查询标签列表
        List<CreativeTag> tags = tagPublicMapper.pageQueryTagsByCursor(cursor, size, keyword);

        // 转换为TagVO列表
        List<TagVO> tagVOs = tags.stream()
                .map(this::buildTagVO)
                .toList();

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setRecords(tagVOs);

        // 设置是否有下一页和下一个游标
        if (!tags.isEmpty()) {
            CreativeTag lastTag = tags.get(tags.size() - 1);
            pageResult.setNextCursor(lastTag.getCreateTime());
            pageResult.setHasNext(tags.size() == size);
        } else {
            pageResult.setHasNext(false);
        }

        // 对于第一页查询，返回总记录数；后续页不返回
        if (cursor == null) {
            long total = tagPublicMapper.countTags(keyword);
            pageResult.setTotal(total);
        } else {
            pageResult.setTotal(-1);
        }

        log.info("标签分页查询成功: 返回记录数={}", tagVOs.size());
        return pageResult;
    }

    /**
     * 根据名称前缀搜索标签（支持分页）
     */
    @Override
    public PageResult searchTagsByPrefix(TagPageQueryDTO pageQueryDTO) {
        log.info("前缀搜索标签: cursor={}, size={}, keyword={}",
                pageQueryDTO.getCursor(), pageQueryDTO.getSize(), pageQueryDTO.getKeyword());

        // 获取分页参数
        int size = pageQueryDTO.getSize() != null ? pageQueryDTO.getSize() : 20;
        LocalDateTime cursor = pageQueryDTO.getCursor();
        String keyword = pageQueryDTO.getKeyword();

        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键词为空，返回空结果
            PageResult emptyResult = new PageResult();
            emptyResult.setRecords(List.of());
            emptyResult.setHasNext(false);
            emptyResult.setTotal(0);
            return emptyResult;
        }

        // 查询标签列表（前缀匹配）
        List<CreativeTag> tags = tagPublicMapper.searchTagsByPrefix(cursor, size, keyword);

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setRecords(tags);

        // 设置是否有下一页和下一个游标
        if (!tags.isEmpty()) {
            CreativeTag lastTag = tags.get(tags.size() - 1);
            // 使用创建时间作为游标
            pageResult.setNextCursor(lastTag.getCreateTime());
            pageResult.setHasNext(tags.size() == size);
        } else {
            pageResult.setHasNext(false);
        }

        // 对于第一页查询，返回总记录数；后续页不返回
        if (cursor == null) {
            long total = tagPublicMapper.countTagsByPrefix(keyword);
            pageResult.setTotal(total);
        } else {
            pageResult.setTotal(-1);
        }

        log.info("标签前缀搜索成功: 返回记录数={}, 关键词={}", tags.size(), keyword);
        return pageResult;
    }

    /**
     * 根据名称搜索标签（兼容旧版本，返回列表）
     */
    @Override
    public List<CreativeTag> searchTagsByName(String keyword) {
        log.info("搜索标签: keyword={}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键词为空，返回热门标签
            return tagPublicMapper.pageQueryTagsByCursor(null, 10, null);
        }

        // 使用前缀匹配搜索，固定返回20条
        return tagPublicMapper.searchTagsByPrefix(null, 20, keyword);
    }

    /**
     * 获取热门标签
     */
    @Override
    public List<TagVO> getHotTags(Integer limit) {
        int actualLimit = limit != null ? limit : 5;
        log.info("获取热门标签: limit={}", actualLimit);

        return tagPublicMapper.getHotTags(actualLimit);
    }

    /**
     * 构建TagVO对象
     */
    private TagVO buildTagVO(CreativeTag tag) {
        return TagVO.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .hotScore(0.0) // 热度评分需要后续实现
                .usageCount(0)  // 使用次数需要后续实现
                .build();
    }

    /**
     * 创建标签（如果不存在）
     */
    @Override
    public CreativeTag createTagIfNotExists(String tagName) {
        log.info("创建标签（如果不存在）: tagName={}", tagName);

        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }

        // 检查标签是否已存在
        CreativeTag existingTag = tagPublicMapper.getByTagName(tagName.trim());
        if (existingTag != null) {
            log.info("标签已存在: tagId={}, tagName={}", existingTag.getTagId(), existingTag.getTagName());
            return existingTag;
        }

        // 创建新标签
        CreativeTag newTag = CreativeTag.builder()
                .tagName(tagName.trim())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .hotScore(0.0)
                .usageCount(0)
                .build();

        tagPublicMapper.insert(newTag);
        log.info("标签创建成功: tagId={}, tagName={}", newTag.getTagId(), newTag.getTagName());

        return newTag;
    }

    /**
     * 根据ID获取标签详情
     */
    @Override
    public CreativeTag getTagById(Long tagId) {
        log.info("根据ID查询标签: tagId={}", tagId);

        if (tagId == null || tagId <= 0) {
            throw new IllegalArgumentException("标签ID不能为空");
        }

        CreativeTag tag = tagPublicMapper.getByTagId(tagId);
        if (tag == null) {
            log.warn("标签不存在: tagId={}", tagId);
            throw new RuntimeException("标签不存在");
        }

        return tag;
    }
}