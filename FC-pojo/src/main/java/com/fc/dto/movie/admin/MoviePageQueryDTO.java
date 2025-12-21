package com.fc.dto.movie.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "电影分页查询数据模型")
public class MoviePageQueryDTO {

    @Schema(description = "游标（最后一条记录的时间戳）", example = "2023-01-01T00:00:00")
    private LocalDateTime cursor;

    @Positive(message = "每页大小必须大于0")
    @Schema(description = "每页大小", example = "20")
    private Integer size = 20;
}