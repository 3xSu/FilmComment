package com.fc.dto.movie.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "修改电影时传递的数据模型")
public class MovieUpdateDTO {

    @NotNull(message = "电影ID不能为空")
    @Schema(description = "电影ID", example = "1")
    private Long movieId;

    @Schema(description = "电影标题", example = "肖申克的救赎")
    private String title;

    @Positive(message = "电影时长必须大于0")
    @Schema(description = "电影时长（分钟）", example = "142")
    private Integer duration;

    @Schema(description = "电影简介", example = "一场关于希望与救赎的故事")
    private String intro;

    @Schema(description = "海报URL", example = "https://example.com/poster.jpg")
    private String posterUrl;

    @Schema(description = "上映日期", example = "1994-09-23")
    private LocalDate releaseDate;
}