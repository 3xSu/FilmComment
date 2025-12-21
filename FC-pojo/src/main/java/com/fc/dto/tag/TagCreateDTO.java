package com.fc.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Schema(description = "标签创建参数")
public class TagCreateDTO {

    @NotBlank(message = "标签名称不能为空")
    @Size(min = 1, max = 50, message = "标签名称长度必须在1-50个字符之间")
    @Schema(description = "标签名称", required = true, example = "科幻")
    private String tagName;
}