package com.fc.enums;

import lombok.Getter;

@Getter
public enum PostTypeEnum {
    // 枚举项：CODE(值, 描述, 是否剧透)
    NORMAL_NO_SPOILER(1, "普通帖（无剧透）", false),
    DEEP_SPOILER(2, "深度讨论帖（有剧透）", true),
    CREATIVE_NO_SPOILER(3, "二创帖（无剧透）", false),
    CREATIVE_SPOILER(4, "二创帖（有剧透）", true);

    private final int code;       // 对应数据库post_type字段值
    private final String desc;    // 类型描述
    private final boolean spoiler;// 是否包含剧透（替代spoilerType判断）

    PostTypeEnum(int code, String desc, boolean spoiler) {
        this.code = code;
        this.desc = desc;
        this.spoiler = spoiler;
    }

    /**
     * 根据code获取枚举实例（替代常量判断）
     * @param code
     * @return
     */
    public static PostTypeEnum getByCode(Integer code) {
        if (code == null) return null;
        for (PostTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的postType：" + code);
    }

    /**
     * 判断是否为剧透类型
     * @param code
     * @return
     */
    public static boolean isSpoiler(Integer code) {
        PostTypeEnum type = getByCode(code);
        return type != null && type.spoiler;
    }

}