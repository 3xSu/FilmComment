package com.fc.controller.admin;

import com.fc.config.CommentAutoCleanupConfig;
import com.fc.result.Result;
import com.fc.service.admin.CommentAdminService;
import com.fc.vo.comment.CommentAdminVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/comment")
@Tag(name = "管理员评论管理接口")
@Slf4j
public class CommentAdminController {

    @Autowired
    private CommentAdminService commentAdminService;

    /**
     * 删除评论（逻辑删除）
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论（逻辑删除）")
    public Result<CommentAdminVO> deleteComment(
            @PathVariable @Parameter(description = "评论ID") Long commentId) {
        log.info("管理员删除评论: commentId={}", commentId);
        CommentAdminVO deletedComment = commentAdminService.deleteComment(commentId);
        return Result.success(deletedComment);
    }

    /**
     * 恢复被删除的评论
     */
    @PutMapping("/{commentId}/restore")
    @Operation(summary = "恢复被删除的评论")
    public Result<CommentAdminVO> restoreComment(
            @PathVariable @Parameter(description = "评论ID") Long commentId) {
        log.info("管理员恢复评论: commentId={}", commentId);
        CommentAdminVO restoredComment = commentAdminService.restoreComment(commentId);
        return Result.success(restoredComment);
    }

    /**
     * 立即物理删除评论
     */
    @DeleteMapping("/{commentId}/permanent")
    @Operation(summary = "立即物理删除评论")
    public Result<String> permanentlyDeleteComment(
            @PathVariable @Parameter(description = "评论ID") Long commentId) {
        log.info("管理员立即物理删除评论: commentId={}", commentId);
        commentAdminService.permanentlyDeleteComment(commentId);
        return Result.success("评论已永久删除");
    }

    /**
     * 手动触发自动清理任务
     */
    @PostMapping("/cleanup/trigger")
    @Operation(summary = "手动触发自动清理任务")
    public Result<String> triggerAutoCleanup() {
        log.info("管理员手动触发评论自动清理任务");
        int cleanedCount = commentAdminService.autoCleanupExpiredComments();
        return Result.success("自动清理任务完成，共清理 " + cleanedCount + " 条过期评论");
    }

    /**
     * 获取自动清理配置
     */
    @GetMapping("/cleanup/config")
    @Operation(summary = "获取自动清理配置")
    public Result<CommentAutoCleanupConfig> getAutoCleanupConfig() {
        CommentAutoCleanupConfig config = commentAdminService.getAutoCleanupConfig();
        return Result.success(config);
    }

    /**
     * 更新自动清理配置
     */
    @PutMapping("/cleanup/config")
    @Operation(summary = "更新自动清理配置")
    public Result<String> updateAutoCleanupConfig(
            @RequestParam @Parameter(description = "自动清理天数") Integer days) {
        if (days == null || days <= 0) {
            return Result.error("自动清理天数必须大于0");
        }

        commentAdminService.updateAutoCleanupConfig(days);
        log.info("管理员更新自动清理配置: {}天", days);
        return Result.success("自动清理配置已更新为 " + days + " 天");
    }
}