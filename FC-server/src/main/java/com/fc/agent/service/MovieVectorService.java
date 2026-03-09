package com.fc.agent.service;

import com.fc.entity.Movie;
import com.fc.mapper.api.MoviePublicMapper;
import com.fc.utils.OllamaUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 电影向量服务
 * 负责电影向量的存储、检索和相似度计算
 */
@Service
@Slf4j
public class MovieVectorService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private OllamaUtil ollamaUtil;
    
    @Autowired
    private MoviePublicMapper moviePublicMapper;
    
    @Autowired
    private RedissonClient redissonClient;
    
    private static final String MOVIE_VECTOR_PREFIX = "movie:vector:";
    private static final String MOVIE_METADATA_PREFIX = "movie:metadata:";
    
    /**
     * 初始化电影向量库
     */
    public void initializeMovieVectors() {
        log.info("开始初始化电影向量库");
        
        // 获取所有电影
        List<Movie> allMovies = moviePublicMapper.getAllMovies();
        
        // 使用 Redisson 的 RMap 存储电影向量
        RMap<Long, List<Double>> movieVectors = redissonClient.getMap("movie_vectors");
        
        for (Movie movie : allMovies) {
            try {
                // 生成电影向量（基于标题和简介）
                String movieText = movie.getTitle() + " " + (movie.getIntro() != null ? movie.getIntro() : "");
                List<Double> vector = ollamaUtil.generateVector(movieText);
                
                // 存储向量到 Redisson RMap
                movieVectors.put(movie.getMovieId(), vector);
                
                // 存储电影元数据
                storeMovieMetadata(movie);
                
                log.debug("电影向量初始化成功: movieId={}, title={}", movie.getMovieId(), movie.getTitle());
                
            } catch (Exception e) {
                log.error("电影向量初始化失败: movieId={}, title={}", movie.getMovieId(), movie.getTitle(), e);
            }
        }
        
        log.info("电影向量库初始化完成，共处理 {} 部电影", allMovies.size());
    }
    
    /**
     * 存储电影向量（备用方法）
     */
    private void storeMovieVector(Long movieId, List<Double> vector) {
        String key = MOVIE_VECTOR_PREFIX + movieId;
        redisTemplate.opsForValue().set(key, vector);
    }
    
    /**
     * 存储电影元数据
     */
    private void storeMovieMetadata(Movie movie) {
        String key = MOVIE_METADATA_PREFIX + movie.getMovieId();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", movie.getTitle());
        metadata.put("avgRating", movie.getAvgRating());
        metadata.put("posterUrl", movie.getPosterUrl());
        metadata.put("releaseDate", movie.getReleaseDate());
        
        redisTemplate.opsForHash().putAll(key, metadata);
    }
    
    /**
     * 计算用户向量与所有电影的相似度
     */
    public Map<Long, BigDecimal> calculateMovieSimilarity(List<Double> userVector) {
        Map<Long, BigDecimal> similarityMap = new HashMap<>();
        
        try {
            // 使用 Redisson 进行向量相似度搜索
            // 获取所有电影向量
            RMap<Long, List<Double>> movieVectors = redissonClient.getMap("movie_vectors");
            
            if (movieVectors.isEmpty()) {
                log.warn("电影向量库为空，使用备用方案");
                return calculateMovieSimilarityFallback(userVector);
            }
            
            // 计算与每个电影向量的相似度
            for (Map.Entry<Long, List<Double>> entry : movieVectors.entrySet()) {
                Long movieId = entry.getKey();
                List<Double> movieVector = entry.getValue();
                
                if (movieVector != null && movieVector.size() == userVector.size()) {
                    BigDecimal similarity = cosineSimilarity(userVector, movieVector);
                    similarityMap.put(movieId, similarity);
                }
            }
            
            // 只返回相似度最高的20个结果
            return similarityMap.entrySet().stream()
                    .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                    .limit(20)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    
        } catch (Exception e) {
            log.error("使用 Redisson 计算电影相似度失败，使用备用方案", e);
            // 降级到基础实现
            return calculateMovieSimilarityFallback(userVector);
        }
    }
    
    /**
     * 备用相似度计算方案（当 RedisSearch 不可用时）
     */
    private Map<Long, BigDecimal> calculateMovieSimilarityFallback(List<Double> userVector) {
        Map<Long, BigDecimal> similarityMap = new HashMap<>();
        
        // 获取所有电影ID
        Set<String> keys = redisTemplate.keys(MOVIE_VECTOR_PREFIX + "*");
        
        if (keys == null || keys.isEmpty()) {
            log.warn("电影向量库为空，请先初始化");
            return similarityMap;
        }
        
        for (String key : keys) {
            try {
                Long movieId = extractMovieIdFromKey(key);
                List<Double> movieVector = (List<Double>) redisTemplate.opsForValue().get(key);
                
                if (movieVector != null && movieVector.size() == userVector.size()) {
                    BigDecimal similarity = cosineSimilarity(userVector, movieVector);
                    similarityMap.put(movieId, similarity);
                }
                
            } catch (Exception e) {
                log.error("计算电影相似度失败: key={}", key, e);
            }
        }
        
        return similarityMap;
    }
    
    /**
     * 余弦相似度计算
     */
    private BigDecimal cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            return BigDecimal.ZERO;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        
        if (normA == 0 || normB == 0) {
            return BigDecimal.ZERO;
        }
        
        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        return BigDecimal.valueOf(similarity).setScale(4, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 从key中提取电影ID
     */
    private Long extractMovieIdFromKey(String key) {
        return Long.parseLong(key.replace(MOVIE_VECTOR_PREFIX, ""));
    }
    
    /**
     * 获取相似度最高的电影
     */
    public List<Long> getTopSimilarMovies(List<Double> userVector, int topK) {
        Map<Long, BigDecimal> similarityMap = calculateMovieSimilarity(userVector);
        
        return similarityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * 将向量转换为字符串格式
     */
    private String vectorToString(List<Double> vector) {
        return vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}