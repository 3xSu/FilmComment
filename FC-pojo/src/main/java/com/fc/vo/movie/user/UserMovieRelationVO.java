package com.fc.vo.movie.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户电影关系返回VO")
public class UserMovieRelationVO {

    private Long userId;
    private Long movieId;
    private Integer relationType;
    private LocalDateTime updateTime;

    // 电影信息
    @Schema(description = "电影标题")
    private String movieTitle;

    @Schema(description = "海报URL")
    private String posterUrl;

    @Schema(description = "平均评分")
    private BigDecimal avgRating;
}
