package com.fc.service.impl.agent;

import com.fc.entity.AgentUserPreferences;
import com.fc.mapper.agent.AgentUserPreferencesMapper;
import com.fc.service.agent.AgentPreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AI Agent用户偏好服务实现类
 * 
 * 负责管理AI Agent从用户对话中提取的偏好信息，包括电影类型偏好、评分范围偏好等
 */
@Slf4j
@Service
public class AgentPreferenceServiceImpl implements AgentPreferenceService {

    @Autowired
    private AgentUserPreferencesMapper agentUserPreferencesMapper;

    @Override
    @Transactional
    public void saveOrUpdatePreferences(AgentUserPreferences userPreferences) {
        try {
            userPreferences.setCreateTime(LocalDateTime.now());
            userPreferences.setUpdateTime(LocalDateTime.now());
            
            agentUserPreferencesMapper.insertOrUpdate(userPreferences);
            log.info("保存或更新用户偏好成功，用户ID: {}", userPreferences.getUserId());
        } catch (Exception e) {
            log.error("保存或更新用户偏好失败，用户ID: {}, 错误: {}", 
                    userPreferences.getUserId(), e.getMessage(), e);
            throw new RuntimeException("保存或更新用户偏好失败", e);
        }
    }

    @Override
    public AgentUserPreferences getPreferencesByUserId(Long userId) {
        try {
            AgentUserPreferences preferences = agentUserPreferencesMapper.selectByUserId(userId);
            if (preferences != null) {
                log.debug("获取用户偏好成功，用户ID: {}", userId);
            } else {
                log.debug("用户偏好不存在，用户ID: {}", userId);
            }
            return preferences;
        } catch (Exception e) {
            log.error("获取用户偏好失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("获取用户偏好失败", e);
        }
    }

    @Override
    @Transactional
    public void deletePreferences(Long userId) {
        try {
            agentUserPreferencesMapper.deleteByUserId(userId);
            log.info("删除用户偏好成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("删除用户偏好失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("删除用户偏好失败", e);
        }
    }

    @Override
    public boolean existsPreferences(Long userId) {
        try {
            boolean exists = agentUserPreferencesMapper.existsByUserId(userId);
            log.debug("检查用户偏好存在性成功，用户ID: {}, 存在: {}", userId, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查用户偏好存在性失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("检查用户偏好存在性失败", e);
        }
    }

    @Override
    @Transactional
    public void updateFavoriteGenres(Long userId, String favoriteGenres) {
        try {
            AgentUserPreferences preferences = getPreferencesByUserId(userId);
            if (preferences == null) {
                preferences = AgentUserPreferences.builder()
                        .userId(userId)
                        .favoriteGenres(favoriteGenres)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
            } else {
                preferences.setFavoriteGenres(favoriteGenres);
                preferences.setUpdateTime(LocalDateTime.now());
            }
            
            agentUserPreferencesMapper.insertOrUpdate(preferences);
            log.info("更新用户喜欢的电影类型成功，用户ID: {}, 类型: {}", userId, favoriteGenres);
        } catch (Exception e) {
            log.error("更新用户喜欢的电影类型失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("更新用户喜欢的电影类型失败", e);
        }
    }

    @Override
    @Transactional
    public void updatePreferredRatingRange(Long userId, String preferredRatingRange) {
        try {
            AgentUserPreferences preferences = getPreferencesByUserId(userId);
            if (preferences == null) {
                preferences = AgentUserPreferences.builder()
                        .userId(userId)
                        .preferredRatingRange(preferredRatingRange)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
            } else {
                preferences.setPreferredRatingRange(preferredRatingRange);
                preferences.setUpdateTime(LocalDateTime.now());
            }
            
            agentUserPreferencesMapper.insertOrUpdate(preferences);
            log.info("更新用户偏好评分范围成功，用户ID: {}, 范围: {}", userId, preferredRatingRange);
        } catch (Exception e) {
            log.error("更新用户偏好评分范围失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("更新用户偏好评分范围失败", e);
        }
    }

    @Override
    @Transactional
    public void updateDislikedMovies(Long userId, String dislikedMovies) {
        try {
            AgentUserPreferences preferences = getPreferencesByUserId(userId);
            if (preferences == null) {
                preferences = AgentUserPreferences.builder()
                        .userId(userId)
                        .dislikedMovies(dislikedMovies)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
            } else {
                preferences.setDislikedMovies(dislikedMovies);
                preferences.setUpdateTime(LocalDateTime.now());
            }
            
            agentUserPreferencesMapper.insertOrUpdate(preferences);
            log.info("更新用户不喜欢的电影成功，用户ID: {}, 电影: {}", userId, dislikedMovies);
        } catch (Exception e) {
            log.error("更新用户不喜欢的电影失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("更新用户不喜欢的电影失败", e);
        }
    }
}