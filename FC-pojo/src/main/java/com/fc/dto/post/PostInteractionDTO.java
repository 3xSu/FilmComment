package com.fc.dto.post;

import com.fc.enums.PostInteractionType;
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
    @Schema(description = "操作类型")
    private PostInteractionType operationType;
}