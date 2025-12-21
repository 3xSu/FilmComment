package com.fc.mapper.user;

import com.fc.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationMapper {

    /**
     * 插入通知
     */
    @Insert("insert into notification(user_id, type, title, content, related_id, related_type, is_read, create_time) " +
            "values(#{userId}, #{type}, #{title}, #{content}, #{relatedId}, #{relatedType}, #{isRead}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notification notification);

    /**
     * 根据用户ID查询未读通知数量
     */
    @Select("select count(*) from notification where user_id = #{userId} and is_read = 0")
    Integer countUnreadByUserId(Long userId);

    /**
     * 根据用户ID分页查询通知
     */
    @Select("select * from notification where user_id = #{userId} " +
            "and (#{cursor} is null or create_time < #{cursor}) " +
            "order by create_time desc limit #{size}")
    List<Notification> selectByUserIdWithCursor(@Param("userId") Long userId,
                                                @Param("cursor") LocalDateTime cursor,
                                                @Param("size") Integer size);


    /**
     * 标记通知为已读
     */
    @Update("update notification set is_read = 1 where id = #{id} and user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量标记通知为已读
     */
    @Update("update notification set is_read = 1 where user_id = #{userId} and is_read = 0")
    int markAllAsRead(Long userId);

    /**
     * 根据关联信息查询是否已存在通知（避免重复通知）
     */
    @Select("select count(*) from notification where user_id = #{userId} and type = #{type} " +
            "and related_id = #{relatedId} and related_type = #{relatedType} " +
            "and create_time > #{startTime}")
    Integer countRecentDuplicate(@Param("userId") Long userId,
                                 @Param("type") Integer type,
                                 @Param("relatedId") Long relatedId,
                                 @Param("relatedType") Integer relatedType,
                                 @Param("startTime") LocalDateTime startTime);
}