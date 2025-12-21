package com.fc.controller.api;


import com.fc.dto.movie.admin.MoviePageQueryDTO;
import com.fc.dto.movie.admin.SearchMovieDTO;
import com.fc.result.PageResult;
import com.fc.result.Result;
import com.fc.service.api.MoviePublicService;
import com.fc.vo.movie.admin.MovieRatingStatsVO;
import com.fc.vo.movie.admin.MovieVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "电影公共接口")
@Slf4j
public class MoviePublicController {

    @Autowired
    private MoviePublicService moviePublicService;

    /**
     * 搜索电影（条件搜索）
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索电影")
    public Result<PageResult> searchMovies(@Validated SearchMovieDTO searchDTO) {
        log.info("搜索电影: {}", searchDTO);
        PageResult pageResult = moviePublicService.searchMovies(searchDTO);
        return Result.success(pageResult);
    }

    /**
     * 分页查询电影（游标分页，无限滚动）
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询电影（游标分页，无限滚动）")
    public Result<PageResult> pageQueryMovies(@Validated MoviePageQueryDTO pageQueryDTO) {
        log.info("分页查询电影，游标: {}, 每页大小: {}", pageQueryDTO.getCursor(), pageQueryDTO.getSize());
        PageResult pageResult = moviePublicService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取电影详情
     * @param movieId 电影ID
     * @return 电影详情
     */
    @GetMapping("/{movieId}")
    @Operation(summary = "根据ID获取电影详情")
    public Result<MovieVO> getMovieById(@PathVariable Long movieId) {
        log.info("获取电影详情: movieId={}", movieId);
        MovieVO movieVO = moviePublicService.getMovieById(movieId);
        return Result.success(movieVO);
    }

    /**
     * 获取指定电影的评分统计
     * @param movieId 电影ID
     * @return 评分统计信息
     */
    @GetMapping("/stats/{movieId}")
    @Operation(summary = "获取电影评分统计")
    public Result<MovieRatingStatsVO> getMovieRatingStats(
            @PathVariable @Parameter(description = "电影ID") Long movieId) {
        log.info("获取电影评分统计: movieId={}", movieId);
        MovieRatingStatsVO stats = moviePublicService.getMovieRatingStats(movieId);
        return Result.success(stats);
    }

    /**
     * 获取所有电影的评分统计列表
     * @return 评分统计列表
     */
    @GetMapping("/stats")
    @Operation(summary = "获取所有电影评分统计")
    public Result<List<MovieRatingStatsVO>> getAllMovieRatingStats() {
        log.info("获取所有电影评分统计");
        List<MovieRatingStatsVO> statsList = moviePublicService.getAllMovieRatingStats();
        return Result.success(statsList);
    }

    /**
     * 根据评分范围筛选电影评分统计
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @return 评分统计列表
     */
    @GetMapping("/stats/range")
    @Operation(summary = "根据评分范围筛选电影评分统计")
    public Result<List<MovieRatingStatsVO>> getMovieRatingStatsByRange(
            @RequestParam @Parameter(description = "最低评分") BigDecimal minRating,
            @RequestParam @Parameter(description = "最高评分") BigDecimal maxRating) {
        log.info("根据评分范围筛选电影评分统计: minRating={}, maxRating={}", minRating, maxRating);
        List<MovieRatingStatsVO> statsList = moviePublicService.getMovieRatingStatsByRange(minRating, maxRating);
        return Result.success(statsList);
    }
}