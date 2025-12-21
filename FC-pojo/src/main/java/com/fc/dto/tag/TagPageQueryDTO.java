package com.fc.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "标签分页查询参数")
public class TagPageQueryDTO {

    @Schema(description = "游标（最后一条记录的创建时间）", example = "")
    private LocalDateTime cursor;

    @Schema(description = "每页记录数", example = "20")
    private Integer size = 20;

    @Schema(description = "搜索关键词")
    private String keyword;
}