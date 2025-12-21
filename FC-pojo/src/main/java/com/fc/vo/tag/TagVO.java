package com.fc.vo.tag;

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
@Schema(description = "标签信息返回的数据格式")
public class TagVO {

    @Schema(description = "标签ID")
    private Long tagId;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "热度评分")
    private Double hotScore;

    @Schema(description = "使用次数")
    private Integer usageCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}