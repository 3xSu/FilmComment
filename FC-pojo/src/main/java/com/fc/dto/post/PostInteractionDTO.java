package com.fc.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Schema(description = "帖子互动操作参数")
public class PostInteractionDTO {

    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", example = "1")
    private Long postId;

    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型：1-点赞，2-收藏", example = "1")
    private Integer operationType;
}