package com.fc.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论管理信息返回的数据格式")
public class CommentAdminVO {

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像URL")
    private String avatarUrl;

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "帖子标题")
    private String postTitle;

    @Schema(description = "父评论ID（0表示评论帖子）")
    private Long parentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "回复数量")
    private Integer replyCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    @Schema(description = "是否已删除：0-未删除，1-已删除")
    private Integer isDeleted;

    @Schema(description = "当前用户是否点赞")
    private Boolean liked;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}