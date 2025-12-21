package com.fc.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "收藏列表分页查询参数")
public class CollectionPageQueryDTO {

    @Schema(description = "游标（最后一条记录的创建时间）", example = "2023-01-01T00:00:00")
    private LocalDateTime cursor;

    @Schema(description = "每页记录数", example = "20")
    private Integer size = 20;
}