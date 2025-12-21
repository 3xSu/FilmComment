package com.fc.dto.movie.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Schema(description = "搜索电影时传递的数据模型")
public class SearchMovieDTO {

    @Schema(description = "电影标题关键字", example = "肖申克")
    private String keyword;

    @Positive(message = "最小时长必须大于0")
    @Schema(description = "最小时长（分钟）", example = "60")
    private Integer minDuration;

    @Positive(message = "最大时长必须大于0")
    @Schema(description = "最大时长（分钟）", example = "180")
    private Integer maxDuration;

    @Schema(description = "上映年份", example = "1994")
    private Integer releaseYear;

    @Schema(description = "排序方式：1-相关性排序，2-评分排序，3-上映时间排序", example = "1")
    private Integer sortType = 1;

    // 添加分页参数
    @PositiveOrZero(message = "页码必须大于等于0")
    @Schema(description = "页码", example = "0")
    private Integer page = 0;

    @Positive(message = "每页大小必须大于0")
    @Schema(description = "每页大小", example = "20")
    private Integer size = 20;
}