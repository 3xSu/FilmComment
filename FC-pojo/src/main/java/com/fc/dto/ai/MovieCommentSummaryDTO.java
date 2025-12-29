package com.fc.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Schema(description = "电影评论AI总结请求DTO")
public class MovieCommentSummaryDTO {

    @NotNull(message = "电影ID不能为空")
    @Schema(description = "电影ID", required = true)
    private Long movieId;

    @Schema(description = "总结类型：1-简洁版，2-详细版", defaultValue = "1")
    private Integer summaryStyle = 1;

    @Schema(description = "最大总结长度", defaultValue = "500")
    private Integer maxLength = 200;

    @Schema(description = "更新阈值：帖子增长数量达到多少时重新生成", defaultValue = "3")
    private Integer updateThreshold;

    @Schema(description = "是否强制重新生成", defaultValue = "false")
    private Boolean forceRefresh = false;

    @NotNull(message = "帖子类型不能为空")
    @Schema(description = "帖子类型筛选：1-无剧透普通，2-有剧透深度，3-二创无剧透，4-二创有剧透")
    private Integer postType;

}