package com.fc.vo.movie.ai;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRecommendVO {
    private List<MovieSimpleVO> recommendMovies;
    private String recommendReason;
    private BigDecimal confidence;
}
