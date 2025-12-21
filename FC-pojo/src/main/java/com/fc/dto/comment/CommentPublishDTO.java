package com.fc.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发表评论时传递的数据模型")
public class CommentPublishDTO {

    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", example = "1")
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", example = "这个帖子写得真好！")
    private String content;

    @Schema(description = "父评论ID（0表示评论帖子，>0表示回复评论）", example = "0")
    private Long parentId = 0L;
}