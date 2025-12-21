package com.fc.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;            // 用户ID
    private String username;        // 用户名
    private String password;        // 密码
    private String avatarUrl;      // 头像URL
    private Integer role;          // 角色：1-普通用户，2-管理员
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
}
