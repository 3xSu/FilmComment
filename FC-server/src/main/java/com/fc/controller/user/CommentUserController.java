package com.fc.controller.user;

import com.fc.dto.comment.CommentPublishDTO;
import com.fc.result.Result;
import com.fc.service.user.CommentUserService;
import com.fc.vo.comment.CommentImageUploadVO;
import com.fc.vo.comment.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user/comment")
@Tag(name = "用户评论相关接口")
@Slf4j
public class CommentUserController {

    @Autowired
    private CommentUserService commentUserService;

    /**
     * 发表评论
     * @param commentPublishDTO 评论信息
     * @return 评论信息
     */
    @PostMapping("/publish")
    @Operation(summary = "发表评论")
    public Result<CommentVO> publishComment(@RequestBody @Validated CommentPublishDTO commentPublishDTO) {
        log.info("发表评论: {}", commentPublishDTO);
        CommentVO commentVO = commentUserService.publishComment(commentPublishDTO);
        return Result.success(commentVO);
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{commentId}")
    @Operation(summary = "删除评论")
    public Result<String> deleteComment(@PathVariable Long commentId) {
        log.info("删除评论: {}", commentId);
        commentUserService.deleteComment(commentId);
        return Result.success("评论删除成功");
    }

    /**
     * 上传评论图片
     * @param commentId 评论ID
     * @param imageFile 图片文件
     * @param sortOrder 图片排序
     * @return 图片上传信息
     */
    @PostMapping(value = "/{commentId}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传评论图片")
    public Result<CommentImageUploadVO> uploadCommentImage(
            @PathVariable Long commentId,
            @Parameter(description = "图片文件", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "1") Integer sortOrder) {

        log.info("上传评论图片: commentId={}, sortOrder={}", commentId, sortOrder);

        try {
            CommentImageUploadVO uploadVO = commentUserService.uploadCommentImage(commentId, imageFile, sortOrder);
            return Result.success(uploadVO);

        } catch (Exception e) {
            log.error("评论图片上传失败: commentId={}", commentId, e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}