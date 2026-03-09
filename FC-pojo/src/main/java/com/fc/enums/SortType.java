package com.fc.enums;

/**
 * 推荐结果排序类型
 */
public enum SortType {
    
    /**
     * 按相似度排序
     */
    SIMILARITY("similarity", "按相似度排序"),
    
    /**
     * 按评分排序
     */
    RATING("rating", "按评分排序"),
    
    /**
     * 按上映时间排序
     */
    RELEASE_TIME("releaseTime", "按上映时间排序"),
    
    /**
     * 按相关度排序
     */
    RELEVANCE("relevance", "按相关度排序"),
    
    /**
     * 综合排序
     */
    COMPREHENSIVE("comprehensive", "综合排序");
    
    private final String value;
    private final String description;
    
    SortType(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
}