package com.fc.enums;

import lombok.Getter;

@Getter
public enum RelatedType {
    POST(1, "帖子"),
    COMMENT(2, "评论"),
    USER(3, "用户"),
    MOVIE(4, "电影");

    private final Integer code;
    private final String desc;

    RelatedType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RelatedType getByCode(Integer code) {
        for (RelatedType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}