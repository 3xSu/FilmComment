package com.fc.vo.movie.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosterUploadVO {
    private Long movieId;
    private String posterUrl;
    private LocalDateTime updateTime;
}