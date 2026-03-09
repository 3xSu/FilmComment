package com.fc.mapper.agent;

import com.fc.annotation.AutoFill;
import com.fc.entity.AgentUserPreferences;
import com.fc.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

/**
 * AI Agent用户偏好Mapper接口
 */
@Mapper
public interface AgentUserPreferencesMapper {

    /**
     * 插入用户偏好记录
     * @param userPreferences 用户偏好记录
     */
    @Insert("INSERT INTO agent_user_preferences(user_id, favorite_genres, preferred_rating_range, disliked_movies, create_time, update_time) " +
            "VALUES(#{userId}, #{favoriteGenres}, #{preferredRatingRange}, #{dislikedMovies}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    void insert(AgentUserPreferences userPreferences);

    /**
     * 更新用户偏好记录
     * @param userPreferences 用户偏好记录
     */
    @Update("UPDATE agent_user_preferences SET favorite_genres = #{favoriteGenres}, " +
            "preferred_rating_range = #{preferredRatingRange}, disliked_movies = #{dislikedMovies}, " +
            "update_time = #{updateTime} WHERE user_id = #{userId}")
    @AutoFill(OperationType.UPDATE)
    void update(AgentUserPreferences userPreferences);

    /**
     * 根据用户ID查询用户偏好
     * @param userId 用户ID
     * @return 用户偏好记录
     */
    @Select("SELECT * FROM agent_user_preferences WHERE user_id = #{userId}")
    AgentUserPreferences selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除用户偏好
     * @param userId 用户ID
     */
    @Delete("DELETE FROM agent_user_preferences WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 检查用户偏好是否存在
     * @param userId 用户ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM agent_user_preferences WHERE user_id = #{userId}")
    boolean existsByUserId(@Param("userId") Long userId);

    /**
     * 插入或更新用户偏好（upsert操作）
     * @param userPreferences 用户偏好记录
     */
    void insertOrUpdate(AgentUserPreferences userPreferences);
}