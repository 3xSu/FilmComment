package com.fc.service.impl.user;

import com.fc.constant.MessageConstant;
import com.fc.dto.movie.user.RatingSubmitDTO;
import com.fc.dto.movie.user.UserMovieRelationDTO;
import com.fc.entity.Movie;
import com.fc.entity.MovieRating;
import com.fc.entity.UserMovieRelation;
import com.fc.exception.MovieNotFoundException;
import com.fc.exception.RatingAlreadyExistsException;
import com.fc.exception.RatingUpdateFailedException;
import com.fc.mapper.admin.MovieAdminMapper;
import com.fc.mapper.api.MovieRatingMapper;
import com.fc.mapper.user.MovieUserMapper;
import com.fc.service.user.MovieUserService;
import com.fc.vo.movie.user.RatingVO;
import com.fc.vo.movie.user.UserMovieRelationVO;
import com.fc.vo.movie.user.UserRelationStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MovieUserServiceImpl implements MovieUserService {

    @Autowired
    private MovieUserMapper movieUserMapper;

    @Autowired
    private MovieAdminMapper movieAdminMapper;

    @Autowired
    private MovieRatingMapper movieRatingMapper;

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 5;
    // 重试间隔（毫秒）
    private static final int RETRY_INTERVAL = 100;

    /**
     * 标记用户与电影关系
     * @param userId
     * @param relationDTO
     * @return
     */
    @Override
    @Transactional
    public UserMovieRelationVO markMovieRelation(Long userId, UserMovieRelationDTO relationDTO) {
        Long movieId = relationDTO.getMovieId();
        Integer relationType = relationDTO.getRelationType();

        // 验证电影是否存在
        Movie movie = movieAdminMapper.getByMovieId(movieId);
        if (movie == null) {
            throw new MovieNotFoundException(MessageConstant.MOVIE_NOT_FOUND);
        }

        // 验证关系类型
        if (relationType != 1 && relationType != 2) {
            throw new IllegalArgumentException("关系类型参数错误");
        }

        // 查询现有关系
        UserMovieRelation existingRelation = movieUserMapper
                .getByUserIdAndMovieId(userId, movieId);

        if (existingRelation == null) {
            // 创建新关系
            UserMovieRelation newRelation = UserMovieRelation.builder()
                    .userId(userId)
                    .movieId(movieId)
                    .relationType(relationType)
                    .build();
            movieUserMapper.insert(newRelation);
        } else {
            // 更新现有关系
            existingRelation.setRelationType(relationType);
            movieUserMapper.update(existingRelation);
        }

        // 获取更新后的关系
        UserMovieRelation updatedRelation = movieUserMapper
                .getByUserIdAndMovieId(userId, movieId);

        return buildUserMovieRelationVO(updatedRelation);
    }

    /**
     * 取消关系的标记
     * @param userId
     * @param movieId
     */
    @Override
    @Transactional
    public void unmarkMovieRelation(Long userId, Long movieId) {
        movieUserMapper.delete(userId, movieId);
    }

    /**
     * 获取用户与电影的关系列表
     * @param userId
     * @param relationType
     * @return
     */
    @Override
    public List<UserMovieRelationVO> getUserMovieRelations(Long userId, Integer relationType) {
        List<UserMovieRelation> relations = movieUserMapper
                .listByUserIdAndType(userId, relationType);

        return relations.stream()
                .map(this::buildUserMovieRelationVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户关系统计
     * @param userId
     * @return
     */
    @Override
    public UserRelationStatsVO getUserRelationStats(Long userId) {
        List<MovieUserMapper.RelationCount> counts =
                movieUserMapper.countByUserId(userId);

        Integer wantToWatchCount = 0;
        Integer watchedCount = 0;

        for (MovieUserMapper.RelationCount count : counts) {
            if (count.getRelationType() == 1) {
                wantToWatchCount = count.getCount();
            } else if (count.getRelationType() == 2) {
                watchedCount = count.getCount();
            }
        }

        return UserRelationStatsVO.builder()
                .wantToWatchCount(wantToWatchCount)
                .watchedCount(watchedCount)
                .build();
    }

    /**
     * 检查用户与该电影关系
     * @param userId
     * @param movieId
     * @return
     */
    @Override
    public Integer checkUserMovieRelation(Long userId, Long movieId) {
        UserMovieRelation relation = movieUserMapper
                .getByUserIdAndMovieId(userId, movieId);
        return relation != null ? relation.getRelationType() : null;
    }

    /**
     * 提交 - 使用乐观锁，用户只能评分一次
     * @param userId 用户ID
     * @param ratingSubmitDTO 评分信息
     * @return 评分结果
     */
    @Override
    @Transactional
    public RatingVO submitRating(Long userId, RatingSubmitDTO ratingSubmitDTO) {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                return doSubmitRating(userId, ratingSubmitDTO);
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                if (retryCount >= MAX_RETRY_COUNT) {
                    throw new RatingUpdateFailedException("评分提交失败，请稍后重试");
                }
                // 指数退避策略
                sleepWithExponentialBackoff(retryCount);
            }
        }
        throw new RatingUpdateFailedException("评分提交失败");
    }

    /**
     * 实际执行评分提交的核心方法
     */
    @Transactional
    protected RatingVO doSubmitRating(Long userId, RatingSubmitDTO ratingSubmitDTO) {
        Long movieId = ratingSubmitDTO.getMovieId();
        BigDecimal newRatingValue = ratingSubmitDTO.getRatingValue();

        // 检查电影是否存在
        Movie movie = movieAdminMapper.getByMovieId(movieId);
        if (movie == null) {
            throw new MovieNotFoundException(MessageConstant.MOVIE_NOT_FOUND);
        }

        // 验证评分范围
        if (newRatingValue.compareTo(BigDecimal.ZERO) < 0 ||
                newRatingValue.compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("评分值必须在0-5之间");
        }

        // 检查是否已评分（使用数据库唯一约束）
        MovieRating existingRating = movieRatingMapper.getByUserIdAndMovieId(userId, movieId);
        if (existingRating != null) {
            throw new RatingAlreadyExistsException("您已经对该电影评过分，无法重复评分");
        }

        // 创建新评分
        MovieRating rating = MovieRating.builder()
                .userId(userId)
                .movieId(movieId)
                .ratingValue(newRatingValue)
                .ratingComment(ratingSubmitDTO.getRatingComment())
                .build();

        // 插入评分记录
        try {
            movieRatingMapper.insert(rating);
        } catch (DuplicateKeyException e) {
            throw new RatingAlreadyExistsException("您已经对该电影评过分，无法重复评分");
        }

        // 原子更新电影评分统计
        int updated = updateMovieRatingStatsAtomically(movieId, movie.getUpdateTime());

        if (updated == 0) {
            // 触发事务回滚，评分记录也会回滚
            throw new OptimisticLockingFailureException("并发更新冲突");
        }

        // 获取最新电影信息
        Movie updatedMovie = movieAdminMapper.getByMovieId(movieId);
        return buildRatingVO(rating, updatedMovie);
    }

    /**
     * 原子更新电影评分统计信息
     */
    private int updateMovieRatingStatsAtomically(Long movieId, LocalDateTime currentUpdateTime) {
        // 计算新的评分统计
        MovieRatingMapper.RatingStats newStats = calculateNewRatingStats(movieId);

        // 使用乐观锁更新电影统计
        return movieRatingMapper.updateMovieRatingStatsWithOptimisticLock(
                movieId,
                newStats.getAvgRating(),
                newStats.getRatingCount(),
                currentUpdateTime);
    }

    /**
     * 指数退避休眠方法
     */
    private void sleepWithExponentialBackoff(int retryCount) {
        try {
            long sleepTime = RETRY_INTERVAL * (1L << (retryCount - 1));
            Thread.sleep(Math.min(sleepTime, 5000)); // 最大休眠5秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RatingUpdateFailedException("评分提交被中断");
        }
    }

    /**
     * 计算新的评分统计
     */
    private MovieRatingMapper.RatingStats calculateNewRatingStats(Long movieId) {
        MovieRatingMapper.RatingStats stats = movieRatingMapper.getRatingStatsByMovieId(movieId);
        if (stats == null) {
            stats = new MovieRatingMapper.RatingStats();
            stats.setAvgRating(BigDecimal.ZERO);
            stats.setRatingCount(0);
        }
        return stats;
    }

    /**
     * 获取用户对电影的评分
     * @param userId 用户ID
     * @param movieId 电影ID
     * @return 评分信息，如果未评分则返回null
     */
    @Override
    public RatingVO getRating(Long userId, Long movieId) {
        MovieRating rating = movieRatingMapper.getByUserIdAndMovieId(userId, movieId);
        if (rating == null) {
            return null;
        }

        Movie movie = movieAdminMapper.getByMovieId(movieId);
        return buildRatingVO(rating, movie);
    }

    /**
     * 获取用户的所有评分列表
     * @param userId 用户ID
     * @return 评分列表
     */
    @Override
    public List<RatingVO> getUserRatings(Long userId) {
        // 查询用户的所有评分记录
        List<MovieRating> ratings = movieRatingMapper.getByUserId(userId);

        if (ratings == null || ratings.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询电影信息以提高性能
        Map<Long, Movie> movieMap = getMovieMap(ratings);

        return ratings.stream()
                .map(rating -> {
                    Movie movie = movieMap.get(rating.getMovieId());
                    return buildRatingVO(rating, movie);
                })
                .sorted((r1, r2) -> r2.getUpdateTime().compareTo(r1.getUpdateTime())) // 按更新时间倒序
                .collect(Collectors.toList());
    }

    /**
     * 构建RelationVO对象
     * @param relation
     * @return
     */
    private UserMovieRelationVO buildUserMovieRelationVO(UserMovieRelation relation) {
        return UserMovieRelationVO.builder()
                .userId(relation.getUserId())
                .movieId(relation.getMovieId())
                .relationType(relation.getRelationType())
                .updateTime(relation.getUpdateTime())
                .build();
    }

    /**
     * 构建RatingVO对象
     * @param rating 评分实体
     * @param movie 电影实体
     * @return RatingVO
     */
    private RatingVO buildRatingVO(MovieRating rating, Movie movie) {
        RatingVO.RatingVOBuilder builder = RatingVO.builder()
                .ratingId(rating.getRatingId())
                .userId(rating.getUserId())
                .movieId(rating.getMovieId())
                .ratingValue(rating.getRatingValue())
                .ratingComment(rating.getRatingComment())
                .createTime(rating.getCreateTime())
                .updateTime(rating.getUpdateTime());

        // 如果电影信息存在，添加电影相关信息
        if (movie != null) {
            builder.movieTitle(movie.getTitle())
                    .posterUrl(movie.getPosterUrl());
        } else {
            // 如果电影不存在，设置默认值
            builder.movieTitle("")
                    .posterUrl("");
        }

        return builder.build();
    }

    /**
     * 批量获取电影信息映射
     * @param ratings 评分列表
     * @return 电影ID到电影对象的映射
     */
    private Map<Long, Movie> getMovieMap(List<MovieRating> ratings) {
        List<Long> movieIds = ratings.stream()
                .map(MovieRating::getMovieId)
                .distinct()
                .collect(Collectors.toList());

        if (movieIds.isEmpty()) {
            return new HashMap<>();
        }

        // 批量查询电影信息
        List<Movie> movies = movieAdminMapper.getByMovieIds(movieIds);
        return movies.stream()
                .collect(Collectors.toMap(Movie::getMovieId, Function.identity()));
    }
}