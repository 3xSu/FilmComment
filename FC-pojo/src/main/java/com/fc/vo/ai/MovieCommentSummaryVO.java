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
@Schema(description = "电影评论AI总结响应VO")
public class MovieCommentSummaryVO {

    @Schema(description = "AI记录ID")
    private Long recordId;

    @Schema(description = "电影ID")
    private Long movieId;

    @Schema(description = "电影标题")
    private String movieTitle;

    @Schema(description = "AI总结内容")
    private String summaryContent;

    @Schema(description = "总结类型：1-简洁版，2-详细版")
    private Integer summaryStyle;

    @Schema(description = "生成时间")
    private LocalDateTime createTime;

    @Schema(description = "总结统计信息")
    private SummaryStats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "总结统计信息")
    public static class SummaryStats {
        @Schema(description = "分析的评论总数")
        private Integer totalComments;

        @Schema(description = "分析的无剧透评论数")
        private Integer noSpoilerComments;

        @Schema(description = "分析的深度讨论评论数")
        private Integer spoilerComments;

        @Schema(description = "正面评价比例")
        private Double positiveRatio;

        @Schema(description = "负面评价比例")
        private Double negativeRatio;

        @Schema(description = "中性评价比例")
        private Double neutralRatio;
    }
}