package com.fc.dto.movie.ai;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class MovieUserViewingDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "观影列表不能为空")
    @Size(min = 3, message = "观影数据至少需要3条")
    private List<MovieViewedDTO> viewedMovies;
}
