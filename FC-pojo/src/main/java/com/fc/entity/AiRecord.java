package com.fc.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecord {
    private Long recordId;
    private Long userId;
    private Long movieId;
    private Integer type; // 1-每日推荐, 2-影评总结, 3-口味分析
    private Integer summaryType; // 1-电影评论总结, 2-智能搜索, 3-每日推荐, 4-口味分析
    private String content;
    private Integer postCount;   // 基于的帖子数量
    private Integer version;     // 版本号
    private Integer threshold;   // 更新阈值
    private LocalDateTime createTime;
    @Schema(description = "帖子类型:1-无剧透普通,2-有剧透深度,3-二创无剧透,4-二创有剧透")
    private Integer postType;
}