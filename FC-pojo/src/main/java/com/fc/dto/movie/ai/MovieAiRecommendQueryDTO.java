package com.fc.dto.movie.ai;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class MovieAiRecommendQueryDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    // 基于观影历史的推荐参数
    private MovieUserViewingDTO userViewing;
    
    // 基于自然语言的推荐参数
    @Size(max = 200, message = "描述长度不能超过200字符")
    private String description;
    
    // 排序类型
    private String sortType;
}
