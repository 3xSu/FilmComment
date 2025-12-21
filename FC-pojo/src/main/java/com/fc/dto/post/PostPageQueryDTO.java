package com.fc.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "帖子分页查询参数")
public class PostPageQueryDTO {

    @Schema(description = "游标（最后一条记录的创建时间）", example = "2023-01-01T00:00:00")
    private LocalDateTime cursor;

    @Schema(description = "每页记录数", example = "20")
    private Integer size = 20;

    @Schema(description = "电影ID筛选", example = "1")
    private Long movieId;

    @Schema(description = "帖子类型筛选：1-无剧透普通，2-有剧透深度，3-二创无剧透，4-二创有剧透", example = "1")
    private Integer postType;

    @Schema(description = "内容形式筛选：1-图文，2-视频", example = "1")
    private Integer contentForm;

    @Schema(description = "剧透类型筛选：1-无剧透区，2-深度讨论区")
    private Integer spoilerType;
}