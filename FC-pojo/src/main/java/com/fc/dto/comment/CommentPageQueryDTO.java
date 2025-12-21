package com.fc.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "评论分页查询数据模型")
public class CommentPageQueryDTO {

    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", example = "1")
    private Long postId;

    @Schema(description = "游标（最后一条评论的创建时间）", example = "2023-01-01T00:00:00")
    private LocalDateTime cursor;

    @Positive(message = "每页大小必须大于0")
    @Schema(description = "每页大小", example = "20")
    private Integer size = 20;
}