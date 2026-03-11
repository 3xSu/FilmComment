package com.fc.service.impl.agent;

import com.fc.entity.Movie;
import com.fc.mapper.api.MoviePublicMapper;
import com.fc.service.agent.MovieVectorService;
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
 * 电影向量服务实现类
 * 负责电影向量的存储、检索和相似度计算
 */
@Service
@Slf4j
public class MovieVectorServiceImpl implements MovieVectorService {
    
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
        
        // 获取所有电影向量
        RMap<Long, List<Double>> movieVectors = redissonClient.getMap("movie_vectors");
        
        for (Map.Entry<Long, List<Double>> entry : movieVectors.entrySet()) {
            try {
                Long movieId = entry.getKey();
                List<Double> movieVector = entry.getValue();
                
                // 计算余弦相似度
                BigDecimal similarity = calculateCosineSimilarity(userVector, movieVector);
                similarityMap.put(movieId, similarity);
                
            } catch (Exception e) {
                log.error("计算电影相似度失败: movieId={}", entry.getKey(), e);
            }
        }
        
        return similarityMap;
    }
    
    /**
     * 计算余弦相似度
     */
    private BigDecimal calculateCosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.size() != vectorB.size()) {
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
     * 根据电影ID获取向量
     */
    public List<Double> getMovieVector(Long movieId) {
        RMap<Long, List<Double>> movieVectors = redissonClient.getMap("movie_vectors");
        return movieVectors.get(movieId);
    }
    
    /**
     * 获取所有电影向量
     */
    public Map<Long, List<Double>> getAllMovieVectors() {
        RMap<Long, List<Double>> movieVectors = redissonClient.getMap("movie_vectors");
        return movieVectors;
    }
    
    /**
     * 检查电影向量库是否已初始化
     */
    public boolean isInitialized() {
        RMap<Long, List<Double>> movieVectors = redissonClient.getMap("movie_vectors");
        return movieVectors != null && !movieVectors.isEmpty();
    }
}