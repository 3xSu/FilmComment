package com.fc.vo.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子统计更新消息")
public class PostStatUpdateVO {
    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "更新时间戳")
    private Long timestamp;
}