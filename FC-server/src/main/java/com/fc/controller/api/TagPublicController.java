package com.fc.controller.api;

import com.fc.dto.tag.TagCreateDTO;
import com.fc.dto.tag.TagPageQueryDTO;
import com.fc.entity.CreativeTag;
import com.fc.result.PageResult;
import com.fc.result.Result;
import com.fc.service.api.TagPublicService;
import com.fc.vo.tag.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "标签公共接口")
@Slf4j
public class TagPublicController {

    @Autowired
    private TagPublicService tagPublicService;

    /**
     * 分页查询标签（游标分页，无限滚动）
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询标签（游标分页，无限滚动）")
    public Result<PageResult> pageQueryTags(@Validated TagPageQueryDTO pageQueryDTO) {
        log.info("分页查询标签，游标: {}, 每页大小: {}, 关键词: {}",
                pageQueryDTO.getCursor(), pageQueryDTO.getSize(), pageQueryDTO.getKeyword());

        PageResult pageResult = tagPublicService.pageQueryTags(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 前缀搜索标签（支持分页，按标签名前缀匹配）
     */
    @GetMapping("/search/prefix")
    @Operation(summary = "前缀搜索标签（按标签名前缀匹配，支持分页）")
    public Result<PageResult> searchTagsByPrefix(@Validated TagPageQueryDTO pageQueryDTO) {
        log.info("前缀搜索标签，游标: {}, 每页大小: {}, 关键词: {}",
                pageQueryDTO.getCursor(), pageQueryDTO.getSize(), pageQueryDTO.getKeyword());

        PageResult pageResult = tagPublicService.searchTagsByPrefix(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 搜索标签（兼容旧版本，返回列表）
     */
    @GetMapping("/search")
    @Operation(summary = "搜索标签（返回列表，兼容旧版本）")
    public Result<List<CreativeTag>> searchTags(
            @RequestParam(required = false)
            @Parameter(description = "搜索关键词") String keyword) {
        log.info("搜索标签: keyword={}", keyword);

        List<CreativeTag> tags = tagPublicService.searchTagsByName(keyword);
        return Result.success(tags);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门标签")
    public Result<List<TagVO>> getHotTags(
            @RequestParam(required = false, defaultValue = "5")
            @Parameter(description = "返回数量") Integer limit) {
        log.info("获取热门标签: limit={}", limit);

        List<TagVO> hotTags = tagPublicService.getHotTags(limit);
        return Result.success(hotTags);
    }

    /**
     * 根据ID获取标签详情
     */
    @GetMapping("/{tagId}")
    @Operation(summary = "根据ID获取标签详情")
    public Result<CreativeTag> getTagById(
            @PathVariable @Parameter(description = "标签ID") Long tagId) {
        log.info("获取标签详情: tagId={}", tagId);

        CreativeTag tag = tagPublicService.getTagById(tagId);
        return Result.success(tag);
    }

    /**
     * 创建标签（如果不存在）
     */
    @PostMapping
    @Operation(summary = "创建标签（如果不存在则创建）")
    public Result<CreativeTag> createTag(@Validated @RequestBody TagCreateDTO createDTO) {
        log.info("创建标签: tagName={}", createDTO.getTagName());

        CreativeTag tag = tagPublicService.createTagIfNotExists(createDTO.getTagName());
        return Result.success(tag);
    }

}