package com.fc.enums;

import lombok.Getter;

@Getter
public enum PostInteractionType {
    LIKE(1, "点赞"),
    COLLECT(2, "收藏");

    private final Integer code;
    private final String description;

    PostInteractionType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PostInteractionType getByCode(Integer code) {
        for(PostInteractionType type : values()){
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
