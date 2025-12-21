package com.fc.controller.admin;

import com.fc.config.PostAutoCleanupConfig;
import com.fc.result.Result;
import com.fc.service.admin.PostAdminService;
import com.fc.vo.post.PostAdminVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/post")
@Tag(name = "管理员帖子管理接口")
@Slf4j
public class PostAdminController {

    @Autowired
    private PostAdminService postAdminService;

    /**
     * 删除帖子（逻辑删除）
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "删除帖子（逻辑删除）")
    public Result<PostAdminVO> deletePost(
            @PathVariable @Parameter(description = "帖子ID") Long postId) {
        log.info("管理员删除帖子: postId={}", postId);
        PostAdminVO deletedPost = postAdminService.deletePost(postId);
        return Result.success(deletedPost);
    }

    /**
     * 恢复被删除的帖子
     */
    @PutMapping("/{postId}/restore")
    @Operation(summary = "恢复被删除的帖子")
    public Result<PostAdminVO> restorePost(
            @PathVariable @Parameter(description = "帖子ID") Long postId) {
        log.info("管理员恢复帖子: postId={}", postId);
        PostAdminVO restoredPost = postAdminService.restorePost(postId);
        return Result.success(restoredPost);
    }

    /**
     * 立即物理删除帖子
     */
    @DeleteMapping("/{postId}/permanent")
    @Operation(summary = "立即物理删除帖子")
    public Result<String> permanentlyDeletePost(
            @PathVariable @Parameter(description = "帖子ID") Long postId) {
        log.info("管理员立即物理删除帖子: postId={}", postId);
        postAdminService.permanentlyDeletePost(postId);
        return Result.success("帖子已永久删除");
    }

    /**
     * 手动触发自动清理任务
     */
    @PostMapping("/cleanup/trigger")
    @Operation(summary = "手动触发自动清理任务")
    public Result<String> triggerAutoCleanup() {
        log.info("管理员手动触发帖子自动清理任务");
        int cleanedCount = postAdminService.autoCleanupExpiredPosts();
        return Result.success("自动清理任务完成，共清理 " + cleanedCount + " 条过期帖子");
    }

    /**
     * 获取自动清理配置
     */
    @GetMapping("/cleanup/config")
    @Operation(summary = "获取自动清理配置")
    public Result<PostAutoCleanupConfig> getAutoCleanupConfig() {
        PostAutoCleanupConfig config = postAdminService.getAutoCleanupConfig();
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

        postAdminService.updateAutoCleanupConfig(days);
        log.info("管理员更新自动清理配置: {}天", days);
        return Result.success("自动清理配置已更新为 " + days + " 天");
    }
}