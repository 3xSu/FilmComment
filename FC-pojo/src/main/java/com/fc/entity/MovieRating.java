package com.fc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRating implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ratingId;
    private Long userId;
    private Long movieId;
    private BigDecimal ratingValue;  // 评分值（1.0-5.0）
    private String ratingComment;   // 评分备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联查询字段
    private String movieTitle;
    private String posterUrl;
}