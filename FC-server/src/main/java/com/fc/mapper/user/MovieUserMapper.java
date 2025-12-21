package com.fc.mapper.user;

import com.fc.annotation.AutoFill;
import com.fc.entity.UserMovieRelation;
import com.fc.enumeration.OperationType;
import lombok.Data;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MovieUserMapper {

    /**
     * 根据用户ID和电影ID查询关系
     */
    @Select("select * from user_movie_relation where user_id = #{userId} and movie_id = #{movieId}")
    UserMovieRelation getByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);

    /**
     * 插入用户电影关系
     */
    @Insert("insert into user_movie_relation(user_id, movie_id, relation_type, create_time, update_time) " +
            "values(#{userId}, #{movieId}, #{relationType}, #{createTime}, #{updateTime})")
    @AutoFill(OperationType.INSERT)
    void insert(UserMovieRelation relation);

    /**
     * 更新用户电影关系
     */
    @Update("update user_movie_relation set relation_type = #{relationType}, update_time = #{updateTime} " +
            "where user_id = #{userId} and movie_id = #{movieId}")
    @AutoFill(OperationType.UPDATE)
    void update(UserMovieRelation relation);

    /**
     * 删除用户电影关系
     */
    @Delete("delete from user_movie_relation where user_id = #{userId} and movie_id = #{movieId}")
    void delete(@Param("userId") Long userId, @Param("movieId") Long movieId);

    /**
     * 根据用户ID和关系类型查询电影列表
     */
    @Select("select umr.*, m.title, m.poster_url, m.avg_rating " +
            "from user_movie_relation umr " +
            "left join movie m on umr.movie_id = m.movie_id " +
            "where umr.user_id = #{userId} and umr.relation_type = #{relationType} " +
            "order by umr.update_time desc")
    List<UserMovieRelation> listByUserIdAndType(@Param("userId") Long userId, @Param("relationType") Integer relationType);

    /**
     * 统计用户的各种关系数量
     */
    @Select("select relation_type, count(*) as count " +
            "from user_movie_relation " +
            "where user_id = #{userId} " +
            "group by relation_type")
    List<RelationCount> countByUserId(@Param("userId") Long userId);

    @Data
    class RelationCount {
        private Integer relationType;
        private Integer count;
    }
}