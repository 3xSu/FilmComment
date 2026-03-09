package com.fc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI Agent用户偏好实体类
 * 对应数据库表：agent_user_preferences
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentUserPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID（唯一）
     */
    private Long userId;

    /**
     * 喜欢的电影类型（JSON格式）
     * 示例：["动作", "科幻", "悬疑"]
     */
    private String favoriteGenres;

    /**
     * 偏好评分范围
     * 示例："7-10"
     */
    private String preferredRatingRange;

    /**
     * 不喜欢的电影（JSON格式）
     * 示例：["恐怖片", "爱情片"]
     */
    private String dislikedMovies;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}