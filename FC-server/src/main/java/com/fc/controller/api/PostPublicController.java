package com.fc.controller.api;

import com.fc.dto.post.PostPageQueryDTO;
import com.fc.result.PageResult;
import com.fc.result.Result;
import com.fc.service.api.PostPublicService;
import com.fc.vo.post.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
@Tag(name = "帖子公共接口")
@Slf4j
public class PostPublicController {

    @Autowired
    private PostPublicService postPublicService;

    /**
     * 分页查询帖子列表（滚动分页）
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询帖子列表（滚动分页）")
    public Result<PageResult> pageQueryPosts(@Validated PostPageQueryDTO pageQueryDTO) {
        log.info("分页查询帖子列表，游标: {}, 每页大小: {}, 电影ID: {}, 帖子类型: {}, 内容形式: {}, 剧透类型: {}",
                pageQueryDTO.getCursor(), pageQueryDTO.getSize(),
                pageQueryDTO.getMovieId(), pageQueryDTO.getPostType(),
                pageQueryDTO.getContentForm(), pageQueryDTO.getSpoilerType());

        PageResult pageResult = postPublicService.pageQueryPosts(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取帖子详情
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/{postId}")
    @Operation(summary = "根据ID获取帖子详情")
    public Result<PostVO> getPostById(
            @PathVariable @Parameter(description = "帖子ID") Long postId) {
        log.info("获取帖子详情: postId={}", postId);
        PostVO postVO = postPublicService.getPostById(postId);
        return Result.success(postVO);
    }
}