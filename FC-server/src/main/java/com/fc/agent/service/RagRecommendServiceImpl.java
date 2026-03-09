package com.fc.agent.service;

import com.fc.context.BaseContext;
import com.fc.exception.RecommendAlgorithmException;
import com.fc.mapper.user.UserMovieRatingMapper;
import com.fc.utils.OllamaUtil;
import com.fc.utils.PromptManager;
import com.fc.vo.movie.ai.MovieRecommendVO;
import com.fc.vo.movie.ai.MovieSimpleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG推荐服务
 * 基于自然语言描述生成电影推荐
 */
@Service
@Slf4j
public class RagRecommendServiceImpl {
    
    @Autowired
    private OllamaUtil ollamaUtil;
    
    @Autowired
    private UserMovieRatingMapper userMovieRatingMapper;
    
    @Autowired
    private MovieVectorService movieVectorService;
    
    @Autowired
    private PromptManager promptManager;
    
    /**
     * 基于自然语言描述推荐电影
     * @param description 电影描述
     * @return 推荐结果
     */
    public MovieRecommendVO recommendByDescription(String description) {
        Long userId = BaseContext.getCurrentId();
        log.info("RAG推荐：userId={}, 描述长度={}", userId, description.length());
        
        try {
            // 1. 调用Ollama API生成用户描述的向量
            List<Double> userVector = generateUserVector(description);
            
            // 2. 使用向量服务计算与所有电影的相似度
            Map<Long, BigDecimal> similarityMap = movieVectorService.calculateMovieSimilarity(userVector);
            
            // 3. 排序并返回结果
            List<Map.Entry<Long, BigDecimal>> sortedEntries = similarityMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(20)
                    .collect(Collectors.toList());
            
            // 批量查询电影信息
            List<Long> recommendMovieIds = sortedEntries.stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            // 查询电影信息
            List<MovieSimpleVO> recommendMovies = userMovieRatingMapper.batchGetMovieInfo(recommendMovieIds);
            
            // 生成推荐理由
            String recommendReason = generateRecommendReason(description);
            
            // 计算置信度
            BigDecimal confidence = calculateConfidence(sortedEntries);
            
            log.info("RAG推荐完成：userId={}, 推荐电影数量={}", userId, recommendMovies.size());
            
            return MovieRecommendVO.builder()
                    .recommendMovies(recommendMovies)
                    .recommendReason(recommendReason)
                    .confidence(confidence)
                    .build();
                    
        } catch (Exception e) {
            userId = BaseContext.getCurrentId();
            log.error("RAG推荐失败：userId={}", userId, e);
            throw new RecommendAlgorithmException("RAG推荐执行异常");
        }
    }
    
    /**
     * 生成用户描述的向量
     * @param description 用户描述
     * @return 向量
     */
    private List<Double> generateUserVector(String description) {
        // 调用Ollama API生成向量
        return ollamaUtil.generateVector(description);
    }
    

    
    /**
     * 计算置信度
     * @param sortedEntries 排序后的相似度条目
     * @return 置信度
     */
    private BigDecimal calculateConfidence(List<Map.Entry<Long, BigDecimal>> sortedEntries) {
        if (sortedEntries.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 计算平均相似度作为置信度
        BigDecimal totalSimilarity = sortedEntries.stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal confidence = totalSimilarity.divide(
                BigDecimal.valueOf(sortedEntries.size()), 
                4, 
                RoundingMode.HALF_UP
        );
        
        return confidence;
    }
    
    /**
     * 生成智能推荐理由
     */
    private String generateRecommendReason(String description) {
        try {
            // 使用优化后的提示词生成推荐理由
            String prompt = promptManager.buildRecommendReasonPrompt(description);
            
            String reason = ollamaUtil.generateText(prompt);
            return reason != null ? reason : String.format("根据您的描述：'%s'，为您推荐了相关电影", description);
            
        } catch (Exception e) {
            log.warn("智能推荐理由生成失败，使用默认理由", e);
            return String.format("根据您的描述：'%s'，为您推荐了相关电影", description);
        }
    }
}
