package com.fc.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论自动清理配置")
public class CommentAutoCleanupConfig {

    @Schema(description = "自动清理天数", example = "7")
    private Integer autoCleanupDays;

    @Schema(description = "是否启用自动清理", example = "true")
    private Boolean enabled;

    @Schema(description = "下次清理时间", example = "2024-01-01T02:00:00")
    private String nextCleanupTime;
}