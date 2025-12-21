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
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;                    // 通知ID
    private Long userId;                // 接收用户ID
    private Integer type;               // 通知类型：1-评论通知，2-点赞通知，3-系统通知
    private String title;               // 通知标题
    private String content;             // 通知内容
    private Long relatedId;             // 关联ID（帖子ID/评论ID）
    private Integer relatedType;        // 关联类型：1-帖子，2-评论
    private Integer isRead;             // 是否已读：0-未读，1-已读
    private LocalDateTime createTime;    // 创建时间
}