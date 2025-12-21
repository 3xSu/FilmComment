package com.fc.vo.post;

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
@Schema(description = "帖子视频上传返回的数据格式")
public class PostVideoUploadVO {

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "视频URL")
    private String videoUrl;

    @Schema(description = "上传时间")
    private LocalDateTime createTime;
}