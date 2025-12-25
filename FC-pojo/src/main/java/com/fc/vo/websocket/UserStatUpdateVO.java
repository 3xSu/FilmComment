package com.fc.vo.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户统计更新消息")
public class UserStatUpdateVO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "已看电影数量")
    private Integer watchedCount;

    @Schema(description = "想看电影数量")
    private Integer wishCount;

    @Schema(description = "更新时间戳")
    private Long timestamp;
}