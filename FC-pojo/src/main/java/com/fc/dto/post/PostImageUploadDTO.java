package com.fc.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "帖子图片上传参数")
public class PostImageUploadDTO {

    @Schema(description = "图片文件", required = true)
    private MultipartFile imageFile;

    @Schema(description = "图片排序（从1开始）", example = "1")
    private Integer sortOrder = 1;
}