package com.fc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreativeTag implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long tagId;          // 标签ID
    private String tagName;      // 标签名称
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
    private Double hotScore;     // 热度评分
    private Integer usageCount;  // 使用次数
}
