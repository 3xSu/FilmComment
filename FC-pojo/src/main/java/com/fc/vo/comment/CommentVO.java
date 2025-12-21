package com.fc.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论信息返回的数据格式")
public class CommentVO {

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

    @Schema(description = "当前用户是否点赞")
    private Boolean liked;

    @Schema(description = "评论图片列表")
    private List<String> imageUrls;
}