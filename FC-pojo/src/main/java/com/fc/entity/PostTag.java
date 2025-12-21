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
public class PostTag implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;            // 关联ID
    private Long postId;        // 帖子ID
    private Long tagId;         // 标签ID
    private LocalDateTime createTime;  // 创建时间
}