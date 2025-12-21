package com.fc.mapper.api;

import com.fc.annotation.AutoFill;
import com.fc.entity.User;
import com.fc.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountMapper {

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    @Select("select * from users where username = #{username}")
    User getByUsername(String username);

    /**
     * 插入新用户
     * @param user
     */
    @Insert("insert into users(username, password, role, avatar_url, create_time, update_time) " +
            "values(#{username}, #{password}, #{role}, #{avatarUrl}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    @AutoFill(OperationType.INSERT)
    void insert(User user);

    /**
     * 根据id查询用户信息
     * @param userId
     * @return
     */
    @Select("select * from users where user_id = #{userId}")
    User getByUserId(Long userId);

    /**
     * 更新用户头像
     * @param user
     */
    @Update("update users set avatar_url = #{avatarUrl}, update_time = #{updateTime} where user_id = #{userId}")
    @AutoFill(OperationType.UPDATE)
    void updateAvatar(User user);
}
