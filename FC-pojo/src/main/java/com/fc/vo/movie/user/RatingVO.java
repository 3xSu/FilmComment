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
@Schema(description = "评分信息返回的数据格式")
public class RatingVO {

    private Long ratingId;
    private Long userId;
    private Long movieId;
    private BigDecimal ratingValue;
    private String ratingComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联信息
    private String movieTitle;
    private String posterUrl;
}
