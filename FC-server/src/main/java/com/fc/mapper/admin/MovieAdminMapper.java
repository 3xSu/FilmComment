package com.fc.mapper.admin;

import com.fc.annotation.AutoFill;
import com.fc.entity.Movie;
import com.fc.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MovieAdminMapper {

    /**
     * 插入新电影
     * @param movie
     */
    @Insert("insert into movie(title, duration, intro, poster_url, release_date, avg_rating, rating_count, is_deleted, create_time, update_time) " +
            "values(#{title}, #{duration}, #{intro}, #{posterUrl}, #{releaseDate}, #{avgRating}, #{ratingCount}, #{isDeleted}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "movieId")
    @AutoFill(OperationType.INSERT)
    void insert(Movie movie);

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
     * 更新电影信息
     * @param movie
     */
    @Update("update movie set title = #{title}, duration = #{duration}, intro = #{intro}, " +
            "poster_url = #{posterUrl}, release_date = #{releaseDate}, update_time = #{updateTime} " +
            "where movie_id = #{movieId} and is_deleted = 0")
    @AutoFill(OperationType.UPDATE)
    void update(Movie movie);

    /**
     * 更新电影海报
     * @param movie
     */
    @Update("update movie set poster_url = #{posterUrl}, update_time = #{updateTime} where movie_id = #{movieId} and is_deleted = 0")
    @AutoFill(OperationType.UPDATE)
    void updatePoster(Movie movie);



//    /**
//     * 分页查询电影（按创建时间倒序）
//     * @param offset
//     * @param size
//     * @return
//     */
//    @Select("select movie_id, title, duration, intro, poster_url, release_date, avg_rating, rating_count " +
//            "from movie where is_deleted = 0 order by create_time desc limit #{offset}, #{size}")
//    List<MovieSearchVO> pageQueryMovies(@Param("offset") Integer offset, @Param("size") Integer size);


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

}