package com.fc.service.impl.api;

import com.fc.constant.MessageConstant;
import com.fc.dto.movie.admin.MoviePageQueryDTO;
import com.fc.dto.movie.admin.SearchMovieDTO;
import com.fc.entity.Movie;
import com.fc.exception.MovieNotFoundException;
import com.fc.mapper.api.MoviePublicMapper;
import com.fc.mapper.api.MovieRatingMapper;
import com.fc.result.PageResult;
import com.fc.service.api.MoviePublicService;
import com.fc.vo.movie.admin.MovieRatingStatsVO;
import com.fc.vo.movie.admin.MovieSearchVO;
import com.fc.vo.movie.admin.MovieVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoviePublicServiceImpl implements MoviePublicService {

    @Autowired
    private MoviePublicMapper moviePublicMapper;
    @Autowired
    private MovieRatingMapper movieRatingMapper;

    /**
     * 构建MovieVO对象
     * @param movie
     * @return
     */
    private MovieVO buildMovieVO(Movie movie) {
        return MovieVO.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .duration(movie.getDuration())
                .intro(movie.getIntro())
                .posterUrl(movie.getPosterUrl())
                .releaseDate(movie.getReleaseDate())
                .avgRating(movie.getAvgRating())
                .ratingCount(movie.getRatingCount())
                .createTime(movie.getCreateTime())
                .updateTime(movie.getUpdateTime())
                .build();
    }

    /**
     * 构建评分统计VO对象
     */
    private MovieRatingStatsVO buildMovieRatingStatsVO(Movie movie,
                                                       MovieRatingMapper.RatingStats ratingStats,
                                                       MovieRatingMapper.RatingDistribution distribution) {
        return MovieRatingStatsVO.builder()
                .movieId(movie.getMovieId())
                .movieTitle(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .avgRating(ratingStats != null ? ratingStats.getAvgRating() : BigDecimal.ZERO)
                .ratingCount(ratingStats != null ? ratingStats.getRatingCount() : 0)
                .star5Count(distribution != null ? distribution.getStar5Count() : 0)
                .star4Count(distribution != null ? distribution.getStar4Count() : 0)
                .star3Count(distribution != null ? distribution.getStar3Count() : 0)
                .star2Count(distribution != null ? distribution.getStar2Count() : 0)
                .star1Count(distribution != null ? distribution.getStar1Count() : 0)
                .build();
    }

    /**
     * 搜索电影（支持条件搜索和相关性排序）
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    @Override
    public PageResult searchMovies(SearchMovieDTO searchDTO) {
        // 计算偏移量
        Integer offset = searchDTO.getPage() * searchDTO.getSize();

        // 查询总记录数
        long total = moviePublicMapper.countMoviesByCondition(searchDTO);

        // 查询当前页数据
        List<MovieSearchVO> records = moviePublicMapper.searchMoviesByCondition(
                searchDTO, offset, searchDTO.getSize());

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        pageResult.setRecords(records);

        return pageResult;
    }

    /**
     * 分页查询电影（适合无限滚动流，一页20条）
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(MoviePageQueryDTO pageQueryDTO) {
        // 获取分页参数
        int size = pageQueryDTO.getSize() != null ? pageQueryDTO.getSize() : 20;
        LocalDateTime cursor = pageQueryDTO.getCursor();

        List<MovieSearchVO> records;
        long total;

        if (cursor == null) {
            // 第一页查询：获取最新的size条记录
            records = moviePublicMapper.pageQueryMoviesByCursor(null, size);
            total = moviePublicMapper.countAllMovies();
        } else {
            // 后续页查询：获取创建时间小于游标的记录
            records = moviePublicMapper.pageQueryMoviesByCursor(cursor, size);
            // 对于游标分页，不需要总记录数，设为-1表示未知
            total = -1;
        }

        // 构建分页结果
        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        pageResult.setRecords(records);

        // 设置是否有下一页和下一个游标
        if (!records.isEmpty()) {
            // 获取最后一条记录的创建时间作为下一个游标
            MovieSearchVO lastRecord = records.get(records.size() - 1);
            pageResult.setNextCursor(lastRecord.getCreateTime());
            pageResult.setHasNext(records.size() == size);
        } else {
            pageResult.setHasNext(false);
        }

        return pageResult;
    }

    /**
     * 根据ID获取电影详情
     * @param movieId 电影ID
     * @return 电影详情
     */
    @Override
    public MovieVO getMovieById(Long movieId) {
        // 检查电影是否存在
        Movie movie = moviePublicMapper.getByMovieId(movieId);
        if (movie == null) {
            throw new MovieNotFoundException(MessageConstant.MOVIE_NOT_FOUND);
        }

        // 构建返回结果
        return buildMovieVO(movie);
    }

    /**
     * 获取电影的评分统计信息
     * @param movieId 电影ID
     * @return 评分统计信息
     */
    @Override
    public MovieRatingStatsVO getMovieRatingStats(Long movieId) {
        // 验证电影是否存在
        Movie movie = moviePublicMapper.getByMovieId(movieId);
        if (movie == null) {
            throw new MovieNotFoundException("电影不存在");
        }

        // 获取基础评分统计
        MovieRatingMapper.RatingStats ratingStats = movieRatingMapper.getRatingStatsByMovieId(movieId);

        // 获取评分分布统计
        MovieRatingMapper.RatingDistribution distribution = movieRatingMapper.getRatingDistributionByMovieId(movieId);

        return buildMovieRatingStatsVO(movie, ratingStats, distribution);
    }

    /**
     * 获取所有电影的评分统计列表
     * @return 评分统计列表
     */
    @Override
    public List<MovieRatingStatsVO> getAllMovieRatingStats() {
        // 获取所有电影
        List<Movie> movies = moviePublicMapper.getAllMovies();

        return movies.stream()
                .map(movie -> {
                    MovieRatingMapper.RatingStats ratingStats = movieRatingMapper.getRatingStatsByMovieId(movie.getMovieId());
                    MovieRatingMapper.RatingDistribution distribution = movieRatingMapper.getRatingDistributionByMovieId(movie.getMovieId());
                    return buildMovieRatingStatsVO(movie, ratingStats, distribution);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据评分范围筛选电影评分统计
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @return 评分统计列表
     */
    @Override
    public List<MovieRatingStatsVO> getMovieRatingStatsByRange(BigDecimal minRating, BigDecimal maxRating) {
        // 根据评分范围获取电影列表
        List<Movie> movies = moviePublicMapper.getMoviesByRatingRange(minRating, maxRating);

        return movies.stream()
                .map(movie -> {
                    MovieRatingMapper.RatingStats ratingStats = movieRatingMapper.getRatingStatsByMovieId(movie.getMovieId());
                    MovieRatingMapper.RatingDistribution distribution = movieRatingMapper.getRatingDistributionByMovieId(movie.getMovieId());
                    return buildMovieRatingStatsVO(movie, ratingStats, distribution);
                })
                .collect(Collectors.toList());
    }
}
