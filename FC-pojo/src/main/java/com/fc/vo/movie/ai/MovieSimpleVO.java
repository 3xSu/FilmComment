package com.fc.vo.movie.ai;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieSimpleVO {
    private Long movieId;
    private String movieName;
    private String posterUrl;
    private Double rating;
    private Integer year;
    private String genres;
    private String directors;
    private String actors;
}
