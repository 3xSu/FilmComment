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
@Schema(description = "帖子自动清理配置")
public class PostAutoCleanupConfig {

    @Schema(description = "自动清理天数")
    private Integer autoCleanupDays;

    @Schema(description = "是否启用自动清理")
    private Boolean enabled;

    @Schema(description = "下次清理时间")
    private String nextCleanupTime;
}