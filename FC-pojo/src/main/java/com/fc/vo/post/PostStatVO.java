package com.fc.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子统计信息")
public class PostStatVO {
    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "最后更新时间")
    private Long lastUpdateTime;
}