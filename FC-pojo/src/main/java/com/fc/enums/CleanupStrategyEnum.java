package com.fc.enums;

import lombok.Getter;

/**
 * AI Agent对话清理策略枚举
 */
@Getter
public enum CleanupStrategyEnum {
    
    /**
     * 基于时间的清理策略
     * 根据数据创建时间进行清理
     */
    TIME_BASED(1, "基于时间的清理策略"),
    
    /**
     * 基于数量的清理策略
     * 保留最近N条记录，清理超出数量的旧记录
     */
    COUNT_BASED(2, "基于数量的清理策略"),
    
    /**
     * 基于活跃度的清理策略
     * 根据用户活跃度进行清理
     */
    ACTIVITY_BASED(3, "基于活跃度的清理策略");

    private final Integer code;
    private final String desc;

    CleanupStrategyEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举实例
     * @param code 枚举代码
     * @return 对应的枚举实例，如果不存在则返回null
     */
    public static CleanupStrategyEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CleanupStrategyEnum strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return null;
    }

    /**
     * 根据枚举名称获取枚举实例
     * @param name 枚举名称
     * @return 对应的枚举实例，如果不存在则返回null
     */
    public static CleanupStrategyEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try {
            return CleanupStrategyEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}