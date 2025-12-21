package com.fc.vo.movie.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "电影评分统计信息")
public class MovieRatingStatsVO {

    @Schema(description = "电影ID", example = "1")
    private Long movieId;

    @Schema(description = "电影标题", example = "流浪地球2")
    private String movieTitle;

    @Schema(description = "海报URL", example = "https://example.com/poster.jpg")
    private String posterUrl;

    @Schema(description = "平均评分", example = "4.5")
    private BigDecimal avgRating;

    @Schema(description = "评分人数", example = "1000")
    private Integer ratingCount;

    @Schema(description = "评分分布 - 5星数量", example = "300")
    private Integer star5Count;

    @Schema(description = "评分分布 - 4星数量", example = "400")
    private Integer star4Count;

    @Schema(description = "评分分布 - 3星数量", example = "200")
    private Integer star3Count;

    @Schema(description = "评分分布 - 2星数量", example = "80")
    private Integer star2Count;

    @Schema(description = "评分分布 - 1星数量", example = "20")
    private Integer star1Count;
}