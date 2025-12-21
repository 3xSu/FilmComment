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
public class PostCollection implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long collectionId;    // 收藏ID
    private Long userId;          // 用户ID
    private Long postId;         // 帖子ID
    private LocalDateTime createTime;  // 创建时间
}