package com.fc.service.impl.user;

import com.fc.service.impl.agent.RagRecommendServiceImpl;
import com.fc.dto.movie.ai.MovieViewedDTO;
import com.fc.exception.InvalidRatingException;
import com.fc.exception.RecommendAlgorithmException;
import com.fc.exception.UserNoViewingDataException;
import com.fc.mapper.user.UserMovieRatingMapper;
import com.fc.service.user.AiRecommendService;
import com.fc.vo.movie.ai.MovieRecommendVO;
import com.fc.vo.movie.ai.MovieSimpleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI电影推荐服务实现
 */
@Service
@Slf4j
public class AiRecommendServiceImpl implements AiRecommendService {
    
    @Value("${fc.ai.recommend.movie.max-recommend-num:20}")
    private Integer maxRecommendNum;
    
    @Value("${fc.ai.recommend.movie.min-view-num:3}")
    private Integer minViewNum;
    
    @Value("${fc.ai.recommend.movie.high-score-threshold:8}")
    private Integer highScoreThreshold;
    
    @Value("${fc.ai.recommend.movie.calculate-timeout:500}")
    private Integer calculateTimeout;
    
    @Autowired
    private UserMovieRatingMapper userMovieRatingMapper;
    
    @Autowired
    private RagRecommendServiceImpl ragRecommendServiceImpl;
    
    /**
     * 基于用户观影历史生成电影推荐
     * @param userId 用户ID
     * @return 推荐结果
     */
    public MovieRecommendVO generateMovieRecommend(Long userId) {
        log.info("生成电影推荐：userId={}", userId);
        
        // 查询用户观影历史
        List<MovieViewedDTO> viewedMovies = userMovieRatingMapper.getUserViewingHistory(userId);
        
        // 校验观影数据
        validateViewingData(viewedMovies);
        
        // 计算电影相似度
        Map<Long, BigDecimal> similarityMap = calculateMovieSimilarity(viewedMovies);
        
        // 过滤已观影电影
        Set<Long> viewedMovieIds = viewedMovies.stream()
                .map(MovieViewedDTO::getMovieId)
                .collect(Collectors.toSet());
        
        // 排序并获取推荐电影
        List<Map.Entry<Long, BigDecimal>> sortedEntries = similarityMap.entrySet().stream()
                .filter(entry -> !viewedMovieIds.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(maxRecommendNum)
                .collect(Collectors.toList());
        
        if (sortedEntries.isEmpty()) {
            throw new RecommendAlgorithmException("无法生成推荐结果");
        }
        
        // 批量查询电影信息
        List<Long> recommendMovieIds = sortedEntries.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        List<MovieSimpleVO> recommendMovies = userMovieRatingMapper.batchGetMovieInfo(recommendMovieIds);
        
        // 生成推荐理由
        String recommendReason = buildRecommendReason(recommendMovies, viewedMovies);
        
        // 计算置信度
        BigDecimal confidence = calculateConfidence(sortedEntries);
        
        log.info("推荐结果生成完成：userId={}, 推荐电影数量={}", userId, recommendMovies.size());
        
        return MovieRecommendVO.builder()
                .recommendMovies(recommendMovies)
                .recommendReason(recommendReason)
                .confidence(confidence)
                .build();
    }
    
    /**
     * 基于自然语言描述生成电影推荐
     * @param userId 用户ID
     * @param description 电影描述
     * @return 推荐结果
     */
    public MovieRecommendVO recommendByDescription(Long userId, String description) {
        log.info("基于自然语言推荐：userId={}, 描述长度={}", userId, description.length());
        
        // 调用RAG推荐服务实现基于自然语言的推荐
        return ragRecommendServiceImpl.recommendByDescription(description);
    }
    
    /**
     * 校验观影数据
     */
    private void validateViewingData(List<MovieViewedDTO> viewedMovies) {
        if (viewedMovies == null || viewedMovies.isEmpty()) {
            throw new UserNoViewingDataException("用户无观影数据");
        }
        
        if (viewedMovies.size() < minViewNum) {
            throw new UserNoViewingDataException("用户观影数据不足，至少需要" + minViewNum + "条观影记录");
        }
        
        // 校验评分范围
        for (MovieViewedDTO viewedMovie : viewedMovies) {
            Integer rating = viewedMovie.getRating();
            if (rating < 1 || rating > 5) {
                throw new InvalidRatingException("评分超出有效范围，应为1-5分");
            }
        }
    }
    
    /**
     * 计算电影相似度
     */
    public Map<Long, BigDecimal> calculateMovieSimilarity(List<MovieViewedDTO> viewedMovies) {
        Map<Long, BigDecimal> similarityMap = new HashMap<>();
        
        // 获取所有电影ID
        List<Long> allMovieIds = userMovieRatingMapper.getAllMovieIds();
        
        // 计算与每个电影的相似度
        for (Long movieId : allMovieIds) {
            BigDecimal similarity = calculateSimilarity(movieId, viewedMovies);
            similarityMap.put(movieId, similarity);
        }
        
        return similarityMap;
    }
    
    /**
     * 计算单个电影的相似度
     */
    private BigDecimal calculateSimilarity(Long targetMovieId, List<MovieViewedDTO> viewedMovies) {
        // 简化的余弦相似度计算
        // 实际项目中可能需要更复杂的算法，考虑电影类型、演员、导演等因素
        int numerator = 0;
        int denominator = 0;
        
        for (MovieViewedDTO viewedMovie : viewedMovies) {
            // 这里使用简单的评分相似度计算
            // 实际项目中应根据电影特征计算相似度
            numerator += viewedMovie.getRating();
            denominator += viewedMovie.getRating() * viewedMovie.getRating();
        }
        
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        
        double similarity = (double) numerator / Math.sqrt(denominator);
        return BigDecimal.valueOf(similarity).setScale(3, RoundingMode.HALF_UP);
    }
    
    /**
     * 生成推荐理由
     */
    public String buildRecommendReason(List<MovieSimpleVO> recommendMovies, List<MovieViewedDTO> viewedMovies) {
        // 找出用户评分最高的电影
        Optional<MovieViewedDTO> highestRatedMovie = viewedMovies.stream()
                .max(Comparator.comparing(MovieViewedDTO::getRating));
        
        if (!highestRatedMovie.isPresent()) {
            return "根据您的观影历史，为您推荐以下电影";
        }
        
        MovieViewedDTO topMovie = highestRatedMovie.get();
        
        return String.format("基于您对电影的喜好，为您推荐了%d部相似风格的电影，希望您喜欢", recommendMovies.size());
    }
    
    /**
     * 计算置信度
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
}
