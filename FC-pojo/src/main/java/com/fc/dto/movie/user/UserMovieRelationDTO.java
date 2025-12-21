package com.fc.dto.movie.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户电影关系操作DTO")
public class UserMovieRelationDTO {

    @NotNull(message = "电影ID不能为空")
    @Schema(description = "电影ID", example = "1")
    private Long movieId;

    @NotNull(message = "关系类型不能为空")
    @Schema(description = "关系类型：1-想看，2-已看过", example = "1")
    private Integer relationType;
}
