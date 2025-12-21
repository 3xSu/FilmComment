package com.fc.vo.post;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@Schema(description = "帖子信息返回的数据格式")
public class PostVO {

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

    @Schema(description = "帖子内容")
    private String content;

    @Schema(description = "帖子类型：1-无剧透普通，2-有剧透深度，3-二创无剧透，4-二创有剧透")
    private Integer postType;

    @Schema(description = "内容形式：1-图文，2-视频")
    private Integer contentForm;

    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "视频URL")
    private String videoUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL) // 仅当该字段不为null时才序列化
    public String getVideoUrl() {
        // 如果是图文帖子，强制返回空字符串或null（根据注解决定是否包含）
        if (this.contentForm == 1) {
            return "";
        }
        return this.videoUrl;
    }

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

    @Schema(description = "标签列表")
    private List<TagVO> tags;

    @Schema(description = "当前用户是否点赞")
    private Boolean liked;

    @Schema(description = "当前用户是否收藏")
    private Boolean collected;
}
