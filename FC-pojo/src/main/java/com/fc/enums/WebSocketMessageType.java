package com.fc.enums;

import lombok.Getter;

@Getter
public enum WebSocketMessageType {
    NOTIFICATION("NOTIFICATION", "通知消息"),
    POST_STAT_UPDATE("POST_STAT_UPDATE", "帖子统计更新"),
    CONNECT_SUCCESS("CONNECT_SUCCESS", "连接成功"),
    PING("PING", "心跳检测"),
    PONG("PONG", "心跳响应"),
    ACK("ACK", "消息确认");

    private final String code;
    private final String desc;

    WebSocketMessageType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WebSocketMessageType getByCode(String code) {
        for (WebSocketMessageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}