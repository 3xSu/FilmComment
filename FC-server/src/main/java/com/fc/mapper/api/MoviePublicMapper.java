package com.fc.mapper.api;

import com.fc.dto.movie.admin.SearchMovieDTO;
import com.fc.entity.Movie;
import com.fc.vo.movie.admin.MovieSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MoviePublicMapper {

    /**
     * 根据电影ID查询电影信息
     * @param movieId
     * @return
     */
    @Select("select * from movie where movie_id = #{movieId} and is_deleted = 0")
    Movie getByMovieId(Long movieId);

    /**
     * 根据电影标题查询电影信息
     * @param title
     * @return
     */
    @Select("select * from movie where title = #{title} and is_deleted = 0")
    Movie getByTitle(String title);

    /**
     * 根据电影标题和排除的电影ID查询电影信息
     * @param title
     * @param excludeMovieId
     * @return
     */
    @Select("select * from movie where title = #{title} and movie_id != #{excludeMovieId} and is_deleted = 0")
    Movie getByTitleExcludeId(@Param("title") String title, @Param("excludeMovieId") Long excludeMovieId);

    /**
     * 根据条件统计电影数量
     * @param condition
     * @return
     */
    long countMoviesByCondition(SearchMovieDTO condition);

    /**
     * 根据条件搜索电影
     * @param condition
     * @param offset
     * @param size
     * @return
     */
    List<MovieSearchVO> searchMoviesByCondition(SearchMovieDTO condition, Integer offset, Integer size);
    /**
     * 统计所有电影数量（用于分页）
     * @return
     */
    @Select("select count(*) from movie where is_deleted = 0")
    long countAllMovies();

    /**
     * 游标分页查询电影（按创建时间倒序）
     * @param cursor 游标时间戳（查询创建时间小于此值的记录）
     * @param size 每页大小
     * @return
     */
    @Select("select movie_id, title, duration, intro, poster_url, release_date, avg_rating, rating_count, create_time " +
            "from movie where is_deleted = 0 " +
            "and (#{cursor} is null or create_time < #{cursor}) " +
            "order by create_time desc limit #{size}")
    List<MovieSearchVO> pageQueryMoviesByCursor(@Param("cursor") LocalDateTime cursor, @Param("size") Integer size);

    /**
     * 根据电影ID列表批量查询电影信息
     */
    @Select({
            "<script>",
            "select * from movie where movie_id in ",
            "<foreach collection='movieIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            " and is_deleted = 0",
            "</script>"
    })
    List<Movie> getByMovieIds(@Param("movieIds") List<Long> movieIds);

    /**
     * 获取所有电影列表
     */
    @Select("select * from movie where is_deleted = 0 order by movie_id")
    List<Movie> getAllMovies();

    /**
     * 根据评分范围获取电影列表
     */
    @Select("select * from movie where is_deleted = 0 and avg_rating between #{minRating} and #{maxRating} order by avg_rating desc")
    List<Movie> getMoviesByRatingRange(@Param("minRating") BigDecimal minRating,
                                       @Param("maxRating") BigDecimal maxRating);
}
