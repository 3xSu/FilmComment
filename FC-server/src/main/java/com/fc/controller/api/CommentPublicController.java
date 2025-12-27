package com.fc.controller.api;

import com.fc.dto.comment.CommentPageQueryDTO;
import com.fc.result.PageResult;
import com.fc.result.Result;
import com.fc.service.api.CommentPublicService;
import com.fc.vo.comment.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论公共接口")
@Slf4j
public class CommentPublicController {

    @Autowired
    private CommentPublicService commentPublicService;

    /**
     * 查询评论列表
     * @param pageQueryDTO
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "获取评论列表（滚动分页）")
    public Result<PageResult> getCommentList(@Validated CommentPageQueryDTO pageQueryDTO) {
        log.info("获取评论列表: postId={}, cursor={}, size={}",
                pageQueryDTO.getPostId(), pageQueryDTO.getCursor(), pageQueryDTO.getSize());
        PageResult pageResult = commentPublicService.getCommentList(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询评论的回复列表
     * @param commentId
     * @return
     */
    @GetMapping("/{commentId}/replies")
    @Operation(summary = "获取评论的回复列表")
    public Result<List<CommentVO>> getCommentReplies(@PathVariable Long commentId) {
        log.info("获取评论回复列表: commentId={}", commentId);
        List<CommentVO> replies = commentPublicService.getCommentReplies(commentId);
        return Result.success(replies);
    }

    /**
     * 根据帖子ID统计评论数量
     * @param postId 帖子ID
     * @param includeReplies 是否包含回复
     * @return 评论数量
     */
    @GetMapping("/count/{postId}")
    @Operation(summary = "根据帖子ID统计评论数量")
    public Result<Integer> countCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(value = "includeReplies", defaultValue = "false") Boolean includeReplies) {
        log.info("统计帖子评论数量: postId={}, includeReplies={}", postId, includeReplies);
        Integer count = commentPublicService.countCommentsByPostId(postId, includeReplies);
        return Result.success(count);
    }
}
