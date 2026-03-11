package com.fc.service.agent;

/**
 * AI助手接口
 * 定义AI助手的能力和方法签名，用于AiServices自动生成实现
 */
public interface AssistantService {
    
    /**
     * 与用户进行电影相关的智能对话
     * 
     * @param message 用户消息
     * @return AI助手回复
     */
    String chat(String message);
    
    /**
     * 基于用户需求推荐电影
     * 
     * @param userRequest 用户请求描述
     * @return 电影推荐结果
     */
    String recommendMovies(String userRequest);
    
    /**
     * 搜索电影信息
     * 
     * @param searchQuery 搜索查询
     * @return 搜索结果
     */
    String searchMovies(String searchQuery);
    
    /**
     * 分析用户观影偏好
     * 
     * @param userDescription 用户描述或对话历史
     * @return 偏好分析结果
     */
    String analyzePreferences(String userDescription);
}