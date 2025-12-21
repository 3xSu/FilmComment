package com.fc.vo.movie.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRelationStatsVO {
    private Integer wantToWatchCount;  // 想看电影数量
    private Integer watchedCount;      // 已看电影数量
}