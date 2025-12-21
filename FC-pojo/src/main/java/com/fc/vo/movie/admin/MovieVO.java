package com.fc.vo.movie.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "电影信息返回的数据格式")
public class MovieVO {

    private Long movieId;
    private String title;
    private Integer duration;
    private String intro;
    private String posterUrl;
    private LocalDate releaseDate;
    private BigDecimal avgRating;
    private Integer ratingCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}