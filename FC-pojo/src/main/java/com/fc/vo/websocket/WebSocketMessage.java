package com.fc.vo.websocket;

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
@Schema(description = "WebSocket消息格式")
public class WebSocketMessage {
    @Schema(description = "消息类型：NOTIFICATION-通知，PING-心跳，ACK-确认")
    private String type;

    @Schema(description = "消息数据")
    private Object data;

    @Schema(description = "时间戳")
    private LocalDateTime timestamp;

    @Schema(description = "消息ID（用于确认）")
    private String messageId;
}