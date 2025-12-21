package com.fc.mapper.user;

import com.fc.annotation.AutoFill;
import com.fc.entity.AiRecord;
import com.fc.enumeration.OperationType;
import lombok.Data;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiRecordMapper {

    /**
     * 插入AI记录
     * @param aiRecord
     */
    @Insert("INSERT INTO ai_record(user_id, movie_id, type, summary_type, content, post_count, version, threshold, post_type, create_time) " +
            "VALUES(#{userId}, #{movieId}, #{type}, #{summaryType}, #{content}, #{postCount}, #{version}, #{threshold}, #{postType}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "recordId")
    @AutoFill(OperationType.INSERT)
    void insert(AiRecord aiRecord);

    /**
     * 根据电影ID和帖子类型查询最新的AI总结记录
     * @param movieId
     * @param postType
     * @return
     */
    @Select("SELECT * FROM ai_record WHERE movie_id = #{movieId} " +
            "AND post_type = #{postType} " +
            "AND type = 2 AND summary_type = 1 ORDER BY create_time DESC LIMIT 1")
    AiRecord getLatestMovieSummaryByPostType(@Param("movieId") Long movieId, @Param("postType") Integer postType);

}