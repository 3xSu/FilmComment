package com.fc.service.agent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 电影向量服务接口
 * 负责电影向量的存储、检索和相似度计算
 */
public interface MovieVectorService {
    
    /**
     * 初始化电影向量库
     */
    void initializeMovieVectors();
    
    /**
     * 计算用户向量与所有电影的相似度
     * 
     * @param userVector 用户向量
     * @return 电影ID与相似度的映射
     */
    Map<Long, BigDecimal> calculateMovieSimilarity(List<Double> userVector);
    
    /**
     * 根据电影ID获取向量
     * 
     * @param movieId 电影ID
     * @return 电影向量
     */
    List<Double> getMovieVector(Long movieId);
    
    /**
     * 获取所有电影向量
     * 
     * @return 电影ID与向量的映射
     */
    Map<Long, List<Double>> getAllMovieVectors();
    
    /**
     * 检查电影向量库是否已初始化
     * 
     * @return 是否已初始化
     */
    boolean isInitialized();
}