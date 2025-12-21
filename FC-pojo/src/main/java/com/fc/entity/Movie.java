package com.fc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long movieId;           // 电影ID
    private String title;          // 电影标题
    private Integer duration;      // 电影时长（分钟）
    private String intro;          // 电影简介
    private String posterUrl;      // 海报URL
    private LocalDate releaseDate; // 上映日期
    private BigDecimal avgRating;  // 平均评分
    private Integer ratingCount;   // 评分人数
    private Integer isDeleted;     // 逻辑删除标志
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
}