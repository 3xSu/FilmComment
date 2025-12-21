package com.fc.vo.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "电影AI总结版本信息")
public class MovieSummaryVersionVO {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "总结内容摘要")
    private String summaryPreview;

    @Schema(description = "基于的帖子数量")
    private Integer postCount;

    @Schema(description = "生成时间")
    private LocalDateTime createTime;

    @Schema(description = "是否为当前版本")
    private Boolean isCurrent;
}
