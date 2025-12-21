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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
