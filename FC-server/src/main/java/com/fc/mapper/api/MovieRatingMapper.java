package com.fc.mapper.api;

import com.fc.annotation.AutoFill;
import com.fc.entity.MovieRating;
import com.fc.enumeration.OperationType;
import lombok.Data;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MovieRatingMapper {
    /**
     * 根据用户ID和电影ID查询评分
     */
    @Select("select * from movie_rating where user_id = #{userId} and movie_id = #{movieId}")
    MovieRating getByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);

    /**
     * 插入评分
     */
    @Insert("insert into movie_rating(user_id, movie_id, rating_value, rating_comment, create_time, update_time) " +
            "values(#{userId}, #{movieId}, #{ratingValue}, #{ratingComment}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "ratingId")
    @AutoFill(OperationType.INSERT)
    void insert(MovieRating rating);

    /**
     * 根据电影ID查询评分统计
     */
    @Select("select " +
            "COALESCE(ROUND(AVG(rating_value), 2), 0.00) as avg_rating, " +
            "COUNT(*) as rating_count " +
            "from movie_rating where movie_id = #{movieId}")
    RatingStats getRatingStatsByMovieId(@Param("movieId") Long movieId);

    /**
     * 使用乐观锁更新电影的平均评分和评分人数
     */
    @Update("update movie set " +
            "avg_rating = #{avgRating}, " +
            "rating_count = #{ratingCount}, " +
            "update_time = NOW() " +
            "where movie_id = #{movieId} and update_time = #{originalUpdateTime}")
    int updateMovieRatingStatsWithOptimisticLock(@Param("movieId") Long movieId,
                                                 @Param("avgRating") BigDecimal avgRating,
                                                 @Param("ratingCount") Integer ratingCount,
                                                 @Param("originalUpdateTime") LocalDateTime originalUpdateTime);

    /**
     * 删除电影评分（用于乐观锁重试失败时的回滚操作）
     */
    @Delete("delete from movie_rating where rating_id = #{ratingId}")
    void deleteRating(@Param("ratingId") Long ratingId);

    /**
     * 评分统计结果类
     */
    @Data
    class RatingStats {
        private BigDecimal avgRating;
        private Integer ratingCount;
    }

    /**
     * 根据用户ID查询所有评分记录
     */
    @Select("select * from movie_rating where user_id = #{userId} order by update_time desc")
    List<MovieRating> getByUserId(@Param("userId") Long userId);

    /**
     * 获取电影的评分分布统计
     */
    @Select("SELECT " +
            "SUM(CASE WHEN rating_value >= 4.5 THEN 1 ELSE 0 END) as star5_count, " +
            "SUM(CASE WHEN rating_value >= 3.5 AND rating_value < 4.5 THEN 1 ELSE 0 END) as star4_count, " +
            "SUM(CASE WHEN rating_value >= 2.5 AND rating_value < 3.5 THEN 1 ELSE 0 END) as star3_count, " +
            "SUM(CASE WHEN rating_value >= 1.5 AND rating_value < 2.5 THEN 1 ELSE 0 END) as star2_count, " +
            "SUM(CASE WHEN rating_value >= 0.5 AND rating_value < 1.5 THEN 1 ELSE 0 END) as star1_count " +
            "FROM movie_rating WHERE movie_id = #{movieId}")
    RatingDistribution getRatingDistributionByMovieId(@Param("movieId") Long movieId);

    /**
     * 评分分布统计结果类
     */
    @Data
    class RatingDistribution {
        private Integer star5Count;  // 4.5-5.0分
        private Integer star4Count;  // 3.5-4.4分
        private Integer star3Count;  // 2.5-3.4分
        private Integer star2Count;  // 1.5-2.4分
        private Integer star1Count;  // 0.5-1.4分
    }
}