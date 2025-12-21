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
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long commentId;        // 评论ID
    private Long userId;          // 用户ID
    private Long postId;         // 帖子ID
    private Long parentId;        // 父评论ID（0表示对帖子的评论）
    private String content;       // 评论内容
    private Integer likeCount;    // 点赞数
    private Integer isDeleted;    // 逻辑删除标志
    private LocalDateTime deleteTime; // 逻辑删除时间
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
}