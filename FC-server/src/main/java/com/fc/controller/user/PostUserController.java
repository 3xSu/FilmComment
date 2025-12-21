package com.fc.controller.user;

import com.fc.dto.post.CollectionPageQueryDTO;
import com.fc.dto.post.PostInteractionDTO;
import com.fc.dto.post.PostPublishDTO;
import com.fc.result.PageResult;
import com.fc.result.Result;
import com.fc.service.user.PostUserService;
import com.fc.vo.post.PostImageUploadVO;
import com.fc.vo.post.PostVO;
import com.fc.vo.post.PostVideoUploadVO;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user/post")
@Tag(name = "用户帖子相关接口")
@Slf4j
public class PostUserController {

    @Autowired
    private PostUserService postUserService;

    /**
     * 发布帖子
     * @param postPublishDTO 帖子发布信息
     * @return 帖子信息
     */
    @PostMapping("/publish")
    @Operation(summary = "发布帖子")
    public Result<PostVO> publishPost(@RequestBody @Validated PostPublishDTO postPublishDTO) {
        log.info("发布帖子: {}", postPublishDTO);
        PostVO postVO = postUserService.publishPost(postPublishDTO);
        return Result.success(postVO);
    }

    /**
     * 点赞帖子
     * @param postInteractionDTO 点赞信息
     * @return 操作结果
     */
    @PostMapping("/like")
    @Operation(summary = "点赞帖子")
    public Result<String> likePost(@RequestBody @Validated PostInteractionDTO postInteractionDTO) {
        log.info("点赞帖子: {}", postInteractionDTO);

        // 验证操作类型
        if (postInteractionDTO.getOperationType() != 1) {
            return Result.error("操作类型错误");
        }

        // 从线程局部变量获取用户ID
        Long userId = com.fc.context.BaseContext.getCurrentId();

        postUserService.likePost(userId, postInteractionDTO.getPostId());
        return Result.success("点赞成功");
    }

    /**
     * 取消点赞帖子
     * @param postInteractionDTO 取消点赞信息
     * @return 操作结果
     */
    @PostMapping("/unlike")
    @Operation(summary = "取消点赞帖子")
    public Result<String> unlikePost(@RequestBody @Validated PostInteractionDTO postInteractionDTO) {
        log.info("取消点赞帖子: {}", postInteractionDTO);

        // 验证操作类型
        if (postInteractionDTO.getOperationType() != 1) {
            return Result.error("操作类型错误");
        }

        // 从线程局部变量获取用户ID
        Long userId = com.fc.context.BaseContext.getCurrentId();

        postUserService.unlikePost(userId, postInteractionDTO.getPostId());
        return Result.success("取消点赞成功");
    }

    /**
     * 检查用户是否点赞过帖子
     * @param postId 帖子ID
     * @return 是否点赞
     */
    @GetMapping("/{postId}/liked")
    @Operation(summary = "检查用户是否点赞过帖子")
    public Result<Boolean> checkUserLikedPost(@PathVariable Long postId) {
        log.info("检查用户是否点赞过帖子: postId={}", postId);

        // 从线程局部变量获取用户ID
        Long userId = com.fc.context.BaseContext.getCurrentId();

        boolean liked = postUserService.checkUserLikedPost(userId, postId);
        return Result.success(liked);
    }

    /**
     * 收藏帖子
     * @param postInteractionDTO 收藏信息
     * @return 操作结果
     */
    @PostMapping("/collect")
    @Operation(summary = "收藏帖子")
    public Result<String> collectPost(@RequestBody @Validated PostInteractionDTO postInteractionDTO) {
        log.info("收藏帖子: {}", postInteractionDTO);

        // 验证操作类型
        if (postInteractionDTO.getOperationType() != 2) {
            return Result.error("操作类型错误");
        }

        // 从线程局部变量获取用户ID
        Long userId = com.fc.context.BaseContext.getCurrentId();

        postUserService.collectPost(userId, postInteractionDTO.getPostId());
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏帖子
     * @param postInteractionDTO 取消收藏信息
     * @return 操作结果
     */
    @PostMapping("/uncollect")
    @Operation(summary = "取消收藏帖子")
    public Result<String> uncollectPost(@RequestBody @Validated PostInteractionDTO postInteractionDTO) {
        log.info("取消收藏帖子: {}", postInteractionDTO);

        // 验证操作类型
        if (postInteractionDTO.getOperationType() != 2) {
            return Result.error("操作类型错误");
        }

        // 从线程局部变量获取用户ID
        Long userId = com.fc.context.BaseContext.getCurrentId();

        postUserService.uncollectPost(userId, postInteractionDTO.getPostId());
        return Result.success("取消收藏成功");
    }

    /**
     * 检查用户是否收藏过帖子
     * @param postId 帖子ID
     * @return 是否收藏
     */
    @GetMapping("/{postId}/collected")
    @Operation(summary = "检查用户是否收藏过帖子")
    public Result<Boolean> checkUserCollectedPost(@PathVariable Long postId) {
        log.info("检查用户是否收藏过帖子: postId={}", postId);

        // 从线程局部变量获取用户ID
        Long userId = com.fc.context.BaseContext.getCurrentId();

        boolean collected = postUserService.checkUserCollectedPost(userId, postId);
        return Result.success(collected);
    }

    /**
     * 获取用户收藏的帖子列表（滚动分页）
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    @GetMapping("/collections")
    @Operation(summary = "获取用户收藏的帖子列表（滚动分页）")
    public Result<PageResult> getUserCollections(@Validated CollectionPageQueryDTO pageQueryDTO) {
        log.info("获取用户收藏的帖子列表，游标: {}, 每页大小: {}",
                pageQueryDTO.getCursor(), pageQueryDTO.getSize());

        // 从线程局部变量获取用户ID
        Long userId = com.fc.context.BaseContext.getCurrentId();

        PageResult pageResult = postUserService.getUserCollections(userId, pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除帖子
     * @param postId 帖子ID
     * @return 操作结果
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "删除帖子")
    public Result<String> deletePost(@PathVariable Long postId) {
        log.info("删除帖子: postId={}", postId);

        // 从线程局部变量获取用户ID
        Long userId = com.fc.context.BaseContext.getCurrentId();

        postUserService.deletePost(userId, postId);
        return Result.success("帖子删除成功");
    }

    /**
     * 上传帖子图片
     * @param postId 帖子ID
     * @param imageFile 图片文件
     * @param sortOrder 图片排序
     * @return 图片上传信息
     */
    @PostMapping(value = "/{postId}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传帖子图片")
    public Result<PostImageUploadVO> uploadPostImage(
            @PathVariable Long postId,
            @Parameter(description = "图片文件", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "1") Integer sortOrder) {

        log.info("上传帖子图片: postId={}, sortOrder={}", postId, sortOrder);

        try {
            // 从线程局部变量获取用户ID
            Long userId = com.fc.context.BaseContext.getCurrentId();

            PostImageUploadVO uploadVO = postUserService.uploadPostImage(userId, postId, imageFile, sortOrder);
            return Result.success(uploadVO);

        } catch (Exception e) {
            log.error("帖子图片上传失败: postId={}", postId, e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传帖子视频
     * @param postId 帖子ID
     * @param videoFile 视频文件
     * @return 视频上传信息
     */
    @PostMapping(value = "/{postId}/upload-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传帖子视频")
    public Result<PostVideoUploadVO> uploadPostVideo(
            @PathVariable Long postId,
            @Parameter(description = "视频文件", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("videoFile") MultipartFile videoFile) {

        log.info("上传帖子视频: postId={}", postId);

        try {
            // 从线程局部变量获取用户ID
            Long userId = com.fc.context.BaseContext.getCurrentId();

            PostVideoUploadVO uploadVO = postUserService.uploadPostVideo(userId, postId, videoFile);
            return Result.success(uploadVO);

        } catch (Exception e) {
            log.error("帖子视频上传失败: postId={}", postId, e);
            return Result.error("视频上传失败: " + e.getMessage());
        }
    }
}