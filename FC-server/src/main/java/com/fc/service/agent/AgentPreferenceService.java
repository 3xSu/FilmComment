package com.fc.service.agent;

import com.fc.entity.AgentUserPreferences;

/**
 * AI Agent用户偏好服务接口
 */
public interface AgentPreferenceService {

    /**
     * 保存或更新用户偏好
     * @param userPreferences 用户偏好记录
     */
    void saveOrUpdatePreferences(AgentUserPreferences userPreferences);

    /**
     * 根据用户ID获取用户偏好
     * @param userId 用户ID
     * @return 用户偏好记录
     */
    AgentUserPreferences getPreferencesByUserId(Long userId);

    /**
     * 删除用户偏好
     * @param userId 用户ID
     */
    void deletePreferences(Long userId);

    /**
     * 检查用户偏好是否存在
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsPreferences(Long userId);

    /**
     * 更新用户喜欢的电影类型
     * @param userId 用户ID
     * @param favoriteGenres 喜欢的电影类型（JSON格式）
     */
    void updateFavoriteGenres(Long userId, String favoriteGenres);

    /**
     * 更新用户偏好评分范围
     * @param userId 用户ID
     * @param preferredRatingRange 偏好评分范围
     */
    void updatePreferredRatingRange(Long userId, String preferredRatingRange);

    /**
     * 更新用户不喜欢的电影
     * @param userId 用户ID
     * @param dislikedMovies 不喜欢的电影（JSON格式）
     */
    void updateDislikedMovies(Long userId, String dislikedMovies);
}