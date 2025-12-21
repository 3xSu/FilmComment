package com.fc.dto.movie.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "用户提交评分的数据模型")
public class RatingSubmitDTO {

    @NotNull(message = "电影ID不能为空")
    @Schema(description = "电影ID", example = "1")
    private Long movieId;

    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "0.0", message = "评分必须大于等于0")
    @DecimalMax(value = "5.0", message = "评分必须小于等于5")
    @Schema(description = "评分值（0-5分，支持小数）", example = "4.5")
    private BigDecimal ratingValue;

    @Schema(description = "评分评论", example = "这部电影很好看")
    private String ratingComment;
}
