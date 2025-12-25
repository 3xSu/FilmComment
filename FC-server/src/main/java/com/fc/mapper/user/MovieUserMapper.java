package com.fc.mapper.user;

import com.fc.annotation.AutoFill;
import com.fc.entity.UserMovieRelation;
import com.fc.enumeration.OperationType;
import lombok.Data;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MovieUserMapper {

    /**
     * 根据用户ID和电影ID查询关系
     * @param userId
     * @param movieId
     * @return
     */
    @Select("select * from user_movie_relation where user_id = #{userId} and movie_id = #{movieId}")
    UserMovieRelation getByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);

    /**
     * 插入用户电影关系
     * @param relation
     */
    @Insert("insert into user_movie_relation(user_id, movie_id, relation_type, create_time, update_time) " +
            "values(#{userId}, #{movieId}, #{relationType}, #{createTime}, #{updateTime})")
    @AutoFill(OperationType.INSERT)
    void insert(UserMovieRelation relation);

    /**
     * 更新用户电影关系
     * @param relation
     */
    @Update("update user_movie_relation set relation_type = #{relationType}, update_time = #{updateTime} " +
            "where user_id = #{userId} and movie_id = #{movieId}")
    @AutoFill(OperationType.UPDATE)
    void update(UserMovieRelation relation);

    /**
     * 删除用户电影关系
     * @param userId
     * @param movieId
     */
    @Delete("delete from user_movie_relation where user_id = #{userId} and movie_id = #{movieId}")
    void delete(@Param("userId") Long userId, @Param("movieId") Long movieId);

    /**
     * 统计用户的各种关系数量
     * @param userId
     * @return
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

    /**
     * 根据用户ID和关系类型查询电影列表
     * @param userId 用户ID
     * @param relationType 关系类型（1-想看电影，2-已看电影，null-查询所有）
     * @return 电影关系列表
     */
    List<UserMovieRelation> listByUserIdAndType(@Param("userId") Long userId,
                                                @Param("relationType") Integer relationType);

    /**
     * 根据用户ID和关系类型分页查询电影列表（游标分页）
     * @param userId 用户ID
     * @param relationType 关系类型
     * @param cursor 游标时间戳
     * @param size 每页大小
     * @return 电影关系列表
     */
    List<UserMovieRelation> listByUserIdAndTypeWithCursor(@Param("userId") Long userId,
                                                          @Param("relationType") Integer relationType,
                                                          @Param("cursor") LocalDateTime cursor,
                                                          @Param("size") Integer size);
}