package com.fc.mapper.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;
import com.fc.dto.movie.ai.MovieViewedDTO;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMovieRatingMapper {
    
    /**
     * 获取用户电影评分记录
     * @param userId 用户ID
     * @return 观影记录列表
     */
    @Select("SELECT movie_id as movieId, rating_value as rating, create_time as viewTime FROM movie_rating WHERE user_id = #{userId}")
    @Options(timeout = 300)
    List<MovieViewedDTO> getUserMovieRatings(Long userId);
    
    /**
     * 获取用户观影记录
     * @param userId 用户ID
     * @return 观影记录列表
     */
    @Select("SELECT umr.movie_id as movieId, mr.rating_value as rating, umr.create_time as viewTime FROM user_movie_relation umr LEFT JOIN movie_rating mr ON umr.movie_id = mr.movie_id AND umr.user_id = mr.user_id WHERE umr.user_id = #{userId} AND umr.relation_type = 2")
    @Options(timeout = 300)
    List<MovieViewedDTO> getUserViewingHistory(Long userId);
    
    /**
     * 批量获取电影信息
     * @param movieIds 电影ID列表
     * @return 电影信息列表
     */
    @Select("<script>" +
            "SELECT movie_id as movieId, title as movieName, poster_url as posterUrl, avg_rating as rating, YEAR(release_date) as year, '' as genres, '' as directors, '' as actors " +
            "FROM movie WHERE movie_id IN " +
            "<foreach item='movieId' collection='movieIds' open='(' separator=',' close=')'>" +
            "#{movieId}" +
            "</foreach>" +
            "</script>")
    @Options(timeout = 300)
    List<Map<String, Object>> getMoviesByIds(List<Long> movieIds);
    
    /**
     * 统计用户评分数量
     * @param userId 用户ID
     * @return 评分数量
     */
    @Select("SELECT COUNT(*) FROM movie_rating WHERE user_id = #{userId}")
    @Options(timeout = 300)
    int countUserMovieRatings(Long userId);
    
    /**
     * 获取所有电影ID
     * @return 电影ID列表
     */
    @Select("SELECT movie_id FROM movie")
    @Options(timeout = 300)
    List<Long> getAllMovieIds();
    
    /**
     * 批量获取电影信息
     * @param movieIds 电影ID列表
     * @return 电影信息列表
     */
    @Select("<script>" +
            "SELECT movie_id as movieId, title as movieName, poster_url as posterUrl, avg_rating as rating, YEAR(release_date) as year, '' as genres, '' as directors, '' as actors " +
            "FROM movie WHERE movie_id IN " +
            "<foreach item='movieId' collection='movieIds' open='(' separator=',' close=')'>" +
            "#{movieId}" +
            "</foreach>" +
            "</script>")
    @Options(timeout = 300)
    List<com.fc.vo.movie.ai.MovieSimpleVO> batchGetMovieInfo(List<Long> movieIds);
}
