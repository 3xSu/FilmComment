package com.fc.service.agent;

import com.fc.vo.movie.ai.MovieRecommendVO;

/**
 * RAG推荐服务接口
 * 基于自然语言描述生成电影推荐
 */
public interface RagRecommendService {
    
    /**
     * 基于自然语言描述推荐电影
     * 
     * @param description 电影描述
     * @return 推荐结果
     */
    MovieRecommendVO recommendByDescription(String description);
}