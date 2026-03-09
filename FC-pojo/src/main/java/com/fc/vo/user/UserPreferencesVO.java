package com.fc.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户偏好信息")
public class UserPreferencesVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "喜欢的电影类型")
    private String favoriteGenres;

    @Schema(description = "偏好评分范围")
    private String preferredRatingRange;

    @Schema(description = "偏好年份范围")
    private String preferredYearRange;

    @Schema(description = "是否有导演偏好")
    private Boolean hasDirectorPreference;

    @Schema(description = "是否有演员偏好")
    private Boolean hasActorPreference;

    @Schema(description = "不喜欢的电影")
    private String dislikedMovies;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 添加喜欢的电影类型
     * 
     * @param genre 电影类型
     */
    public void addPreferredGenre(String genre) {
        if (favoriteGenres == null) {
            favoriteGenres = genre;
        } else {
            favoriteGenres += "," + genre;
        }
    }

    /**
     * 设置偏好评分范围
     * 
     * @param rating 评分范围：high/medium/low
     */
    public void setPreferredRating(String rating) {
        this.preferredRatingRange = rating;
    }

    /**
     * 获取偏好评分范围
     * 
     * @return 评分范围
     */
    public String getPreferredRating() {
        return this.preferredRatingRange;
    }

    /**
     * 获取喜欢的电影类型列表
     * 
     * @return 类型列表
     */
    public List<String> getPreferredGenres() {
        List<String> genres = new ArrayList<>();
        if (favoriteGenres != null && !favoriteGenres.isEmpty()) {
            String[] genreArray = favoriteGenres.split(",");
            for (String genre : genreArray) {
                if (genre != null && !genre.trim().isEmpty()) {
                    genres.add(genre.trim());
                }
            }
        }
        return genres;
    }
}