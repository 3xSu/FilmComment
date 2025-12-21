package com.fc.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    COMMENT(1, "评论通知"),
    LIKE(2, "点赞通知"),
    SYSTEM(3, "系统通知");

    private final Integer code;
    private final String desc;

    NotificationType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据code获取枚举
    public static NotificationType getByCode(Integer code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}