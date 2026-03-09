package com.fc.service.user;

import com.fc.vo.movie.ai.MovieRecommendVO;

/**
 * AI电影推荐服务接口
 */
public interface AiRecommendService {
    
    /**
     * 基于用户观影历史生成电影推荐
     * @param userId 用户ID
     * @return 推荐结果
     */
    MovieRecommendVO generateMovieRecommend(Long userId);
    
    /**
     * 基于自然语言描述生成电影推荐
     * @param userId 用户ID
     * @param description 电影描述
     * @return 推荐结果
     */
    MovieRecommendVO recommendByDescription(Long userId, String description);
}
