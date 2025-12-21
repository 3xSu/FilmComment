package com.fc.vo.ai;

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
@Schema(description = "电影AI总结统计信息")
public class MovieSummaryStatsVO {

    @Schema(description = "电影ID")
    private Long movieId;

    @Schema(description = "总结版本数量")
    private Integer versionCount;

    @Schema(description = "最新版本号")
    private Integer latestVersion;

    @Schema(description = "首次生成时间")
    private LocalDateTime firstCreateTime;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdateTime;

    @Schema(description = "基于的帖子总数")
    private Integer totalPostCount;
}