package com.fc.service.user;

import com.fc.dto.movie.user.RatingSubmitDTO;
import com.fc.dto.movie.user.UserMovieRelationDTO;
import com.fc.dto.movie.user.UserMovieRelationPageQueryDTO;
import com.fc.result.PageResult;
import com.fc.vo.movie.user.RatingVO;
import com.fc.vo.movie.user.UserMovieRelationVO;
import com.fc.vo.movie.user.UserRelationStatsVO;

import java.util.List;

public interface MovieUserService {
    /**
     * 标记观影状态
     */
    UserMovieRelationVO markMovieRelation(Long userId, UserMovieRelationDTO relationDTO);

    /**
     * 取消标记
     */
    void unmarkMovieRelation(Long userId, Long movieId);

    /**
     * 获取用户的电影关系列表
     */
    List<UserMovieRelationVO> getUserMovieRelations(Long userId, Integer relationType);

    /**
     * 获取用户的关系统计
     */
    UserRelationStatsVO getUserRelationStats(Long userId);

    /**
     * 检查用户对电影的关系状态
     */
    Integer checkUserMovieRelation(Long userId, Long movieId);

    /**
     * 提交/更新评分
     * @param userId 用户ID
     * @param ratingSubmitDTO 评分信息
     * @return 评分结果
     */
    RatingVO submitRating(Long userId, RatingSubmitDTO ratingSubmitDTO);

    /**
     * 获取用户对电影的评分
     * @param userId 用户ID
     * @param movieId 电影ID
     * @return 评分信息，如果未评分则返回null
     */
    RatingVO getRating(Long userId, Long movieId);

    /**
     * 获取用户的所有评分列表
     * @param userId 用户ID
     * @return 评分列表
     */
    List<RatingVO> getUserRatings(Long userId);

    /**
     * 获取用户想看电影列表（分页）
     * @param userId 用户ID
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    PageResult getWantToWatchMovies(Long userId, UserMovieRelationPageQueryDTO pageQueryDTO);

    /**
     * 获取用户已看电影列表（分页）
     * @param userId 用户ID
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    PageResult getWatchedMovies(Long userId, UserMovieRelationPageQueryDTO pageQueryDTO);
}
