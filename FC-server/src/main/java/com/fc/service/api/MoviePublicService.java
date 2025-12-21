package com.fc.service.api;

import com.fc.dto.movie.admin.MoviePageQueryDTO;
import com.fc.dto.movie.admin.SearchMovieDTO;
import com.fc.result.PageResult;
import com.fc.vo.movie.admin.MovieRatingStatsVO;
import com.fc.vo.movie.admin.MovieVO;

import java.math.BigDecimal;
import java.util.List;

public interface MoviePublicService {

    /**
     * 搜索电影（支持分页和相关性排序）
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    PageResult searchMovies(SearchMovieDTO searchDTO);

    /**
     * 分页查询电影（适合无限滚动流）
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    PageResult pageQuery(MoviePageQueryDTO pageQueryDTO);

    /**
     * 根据ID获取电影详情
     * @param movieId
     * @return
     */
    MovieVO getMovieById(Long movieId);

    /**
     * 获取电影的评分统计信息
     * @param movieId 电影ID
     * @return 评分统计信息
     */
    MovieRatingStatsVO getMovieRatingStats(Long movieId);

    /**
     * 获取所有电影的评分统计列表
     * @return 评分统计列表
     */
    List<MovieRatingStatsVO> getAllMovieRatingStats();

    /**
     * 根据评分范围筛选电影评分统计
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @return 评分统计列表
     */
    List<MovieRatingStatsVO> getMovieRatingStatsByRange(BigDecimal minRating, BigDecimal maxRating);
}
