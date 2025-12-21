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
@Schema(description = "评论图片上传返回的数据格式")
public class CommentImageUploadVO {

    @Schema(description = "图片ID")
    private Long imageId;

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "图片排序")
    private Integer sortOrder;

    @Schema(description = "上传时间")
    private LocalDateTime createTime;
}