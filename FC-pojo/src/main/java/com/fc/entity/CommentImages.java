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
public class CommentImages implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;              // 图片ID
    private Long commentId;       // 评论ID
    private String imageUrl;      // 图片URL
    private Integer sortOrder;    // 排序顺序
    private LocalDateTime createTime;  // 创建时间
}