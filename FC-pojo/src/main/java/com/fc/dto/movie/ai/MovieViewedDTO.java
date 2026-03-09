package com.fc.dto.movie.ai;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class MovieViewedDTO {
    @NotNull(message = "电影ID不能为空")
    private Long movieId;
    
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小值为1")
    @Max(value = 5, message = "评分最大值为5")
    private Integer rating;
    
    private LocalDateTime viewTime;
}
