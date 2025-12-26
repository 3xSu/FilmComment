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
        log.info("分页查询帖子列表，游标: {}, 每页大小: {}, 电影ID: {}, 帖子类型: {}, 内容形式: {}",
                pageQueryDTO.getCursor(), pageQueryDTO.getSize(),
                pageQueryDTO.getMovieId(), pageQueryDTO.getPostType(),
                pageQueryDTO.getContentForm());

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

    /**
     * 根据用户ID查询帖子列表
     * @param userId 用户ID
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询帖子列表")
    public Result<PageResult> getPostsByUserId(
            @PathVariable Long userId,
            @Validated PostPageQueryDTO pageQueryDTO) {
        log.info("根据用户ID查询帖子: userId={}, 参数={}", userId, pageQueryDTO);
        PageResult pageResult = postPublicService.pageQueryPostsByUserId(userId, pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 统计用户发布的帖子数量
     * @param userId 用户ID
     * @return 帖子数量
     */
    @GetMapping("/user/count")
    @Operation(summary = "统计用户发布的帖子数量")
    public Result<Integer> countUserPosts(
            @RequestParam @Parameter(description = "用户ID") Long userId) {
        log.info("统计用户帖子数量: userId={}", userId);
        int count = postPublicService.countUserPosts(userId);
        return Result.success(count);
    }

    /**
     * 根据电影ID查询所有帖子总数量
     * @param movieId
     * @return
     */
    @GetMapping("/count/movie/{movieId}")
    @Operation(summary = "根据电影ID查询帖子数量")
    public Result<Integer> getPostCountByMovieId(@PathVariable Long movieId) {
        log.info("根据电影ID查询帖子数量: movieId={}", movieId);
        Integer count = postPublicService.countPostsByMovieId(movieId);
        return Result.success(count);
    }
}