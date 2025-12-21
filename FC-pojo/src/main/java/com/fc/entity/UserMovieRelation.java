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
public class UserMovieRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long movieId;
    private Integer relationType; // 1-想看，2-已看过
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联查询字段
    private String title;
    private String posterUrl;
    private BigDecimal avgRating;
}