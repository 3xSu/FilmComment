package com.fc.vo.post;

import com.fc.vo.tag.TagVO;
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
@Schema(description = "帖子搜索返回的数据格式")
public class PostSearchVO {

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像URL")
    private String avatarUrl;

    @Schema(description = "电影ID")
    private Long movieId;

    @Schema(description = "电影标题")
    private String movieTitle;

    @Schema(description = "帖子标题")
    private String title;

    @Schema(description = "帖子内容摘要（前100字符）")
    private String contentSummary;

    @Schema(description = "帖子类型：1-无剧透普通，2-有剧透深度，3-二创无剧透，4-二创有剧透")
    private Integer postType;

    @Schema(description = "内容形式：1-图文，2-视频")
    private Integer contentForm;

    @Schema(description = "封面图片URL（图文帖子的第一张图片或视频帖子的封面）")
    private String coverImage;

    @Schema(description = "视频URL")
    private String videoUrl;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "标签列表（前3个标签）")
    private List<TagVO> tags;
}