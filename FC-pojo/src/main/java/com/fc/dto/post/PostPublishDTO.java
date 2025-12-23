package com.fc.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发布帖子时传递的数据模型")
public class PostPublishDTO {

    @NotNull(message = "电影ID不能为空")
    @Schema(description = "电影ID", example = "1")
    private Long movieId;

    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    @Schema(description = "帖子标题", example = "这部电影太精彩了！")
    private String title;

    @Schema(description = "帖子内容", example = "这是一篇详细的影评内容...")
    private String content;

    @NotNull(message = "帖子类型不能为空")
    @Min(value = 1, message = "帖子类型参数错误")
    @Max(value = 4, message = "帖子类型参数错误")
    @Schema(description = "帖子类型：1-无剧透普通，2-有剧透深度，3-二创无剧透，4-二创有剧透", example = "1")
    private Integer postType;

    @NotNull(message = "内容形式不能为空")
    @Min(value = 1, message = "内容形式参数错误")
    @Max(value = 2, message = "内容形式参数错误")
    @Schema(description = "内容形式：1-图文，2-视频", example = "1")
    private Integer contentForm;

    @Schema(description = "现有标签ID列表", example = "[]")
    private List<Long> tagIds;

    @Schema(description = "新标签名称列表（会自动创建）", example = "[]")
    @Size(max = 10, message = "一次性最多创建10个新标签")
    private List<@Size(min = 1, max = 50, message = "标签名称长度必须在1-50个字符之间") String> newTagNames;

    @Schema(description = "图片URL列表（图文帖子使用）", example = "")
    private List<String> imageUrls;

    @Schema(description = "视频URL（视频帖子使用）", example = "")
    private String videoUrl;

    @Schema(description = "有效的帖子发布示例")
    public static class ValidExample {
        public static final String GRAPHICAL_POST = """
            {
                "movieId": 1,
                "title": "这部电影太精彩了！",
                "content": "这是一篇详细的影评内容，讲述了电影的精彩之处...",
                "postType": 1,
                "contentForm": 1,
                "tagIds": [],
                "newTagNames": ["悬疑", "科幻"],
                "imageUrls": ["https://example.com/image1.jpg"],
                "videoUrl": null
            }
            """;

        public static final String VIDEO_POST = """
            {
                "movieId": 2,
                "title": "电影幕后花絮分享",
                "content": "分享一些有趣的幕后故事",
                "postType": 1,
                "contentForm": 2,
                "tagIds": [],
                "newTagNames": ["幕后"],
                "imageUrls": null,
                "videoUrl": "https://example.com/video.mp4"
            }
            """;
    }

    // 自定义验证逻辑
    @Schema(hidden = true)
    @AssertTrue(message = "图文帖子必须包含内容或图片")
    public boolean isValidContentForm() {
        if (contentForm == 1) {
            // 图文帖子：必须有内容或图片
            return (content != null && !content.trim().isEmpty()) ||
                    (imageUrls != null && !imageUrls.isEmpty());
        } else if (contentForm == 2) {
            // 视频帖子：必须有视频URL
            return videoUrl != null && !videoUrl.trim().isEmpty();
        }
        return true;
    }

    @Schema(hidden = true)
    @AssertTrue(message = "图文帖子不能包含视频，视频帖子不能包含图片")
    public boolean isMediaConsistent() {
        if (contentForm == 1) {
            // 图文帖子：videoUrl必须为空
            return videoUrl == null || videoUrl.trim().isEmpty();
        } else if (contentForm == 2) {
            // 视频帖子：imageUrls必须为空
            return imageUrls == null || imageUrls.isEmpty();
        }
        return true;
    }

    @Schema(hidden = true)
    @AssertTrue(message = "视频帖子必须上传视频文件")
    public boolean isValidVideoPost() {
        if (contentForm == 2) {
            // 视频帖子：必须有视频文件（通过上传接口上传）
            // 这里主要验证DTO中的videoUrl不为空，实际视频文件通过上传接口处理
            return videoUrl != null && !videoUrl.trim().isEmpty();
        }
        return true;
    }


    /**
     * 根据帖子类型判断是否为二创帖子
     * @return true-二创帖子，false-普通帖子
     */
    @Schema(hidden = true)
    public boolean isCreativePost() {
        return postType == 3 || postType == 4; // 3-二创无剧透，4-二创有剧透
    }
}