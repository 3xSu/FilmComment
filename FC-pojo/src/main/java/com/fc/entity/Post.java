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
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long postId;           // 帖子ID
    private Long userId;          // 用户ID
    private Long movieId;         // 电影ID
    private String title;         // 帖子标题
    private String content;       // 帖子内容
    private Integer postType;     // 帖子类型：1-无剧透普通，2-有剧透深度，3-二创无剧透，4-二创有剧透
    private Integer contentForm;  // 内容形式：1-图文，2-视频
    private String videoUrl;      // 视频URL
    private Integer viewCount;    // 浏览量
    private Integer likeCount;    // 点赞数
    private Integer collectCount; // 收藏数
    private Integer isDeleted;    // 逻辑删除标志
    private LocalDateTime deleteTime; // 逻辑删除时间
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
    private LocalDateTime collectionTime; // 收藏时间
}