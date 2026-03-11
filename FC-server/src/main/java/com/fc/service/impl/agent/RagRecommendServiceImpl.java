package com.fc.service.impl.agent;

import com.fc.context.BaseContext;
import com.fc.exception.RecommendAlgorithmException;
import com.fc.mapper.user.UserMovieRatingMapper;
import com.fc.service.agent.RagRecommendService;
import com.fc.utils.OllamaUtil;
import com.fc.utils.PromptManager;
import com.fc.vo.movie.ai.MovieRecommendVO;
import com.fc.vo.movie.ai.MovieSimpleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG推荐服务实现类
 * 基于自然语言描述生成电影推荐
 */
@Service
@Slf4j
public class RagRecommendServiceImpl implements RagRecommendService {
    
    @Autowired
    private OllamaUtil ollamaUtil;
    
    @Autowired
    private UserMovieRatingMapper userMovieRatingMapper;
    
    @Autowired
    private MovieVectorServiceImpl movieVectorServiceImpl;
    
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
            Map<Long, BigDecimal> similarityMap = movieVectorServiceImpl.calculateMovieSimilarity(userVector);
            
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
     * 生成推荐理由
     * @param description 用户描述
     * @return 推荐理由
     */
    private String generateRecommendReason(String description) {
        // 使用Ollama API生成推荐理由
        String prompt = buildRecommendReasonPrompt(description);
        return generateTextWithOllama(prompt);
    }
    
    /**
     * 构建推荐理由提示词
     * @param description 用户描述
     * @return 提示词
     */
    private String buildRecommendReasonPrompt(String description) {
        return String.format("""
            用户描述：%s
            
            请基于这个描述，生成一段友好自然的电影推荐理由。
            
            推荐基本原则：
            1. 准确性第一：只推荐真实存在的电影，绝不虚构电影名称
            2. 类型匹配：确保推荐电影与用户请求的类型一致
            3. 热门优先：当用户要求"热门"电影时，优先推荐高评分、高知名度的作品
            
            内容要求：
            1. 每部电影必须包含：
               - 简单剧情梗概（1-2句话概括核心故事）
               - 推荐理由（为什么适合用户当前需求）
               - 电影特色亮点（突出与众不同之处）
            
            语气要求：
            1. 使用友好、自然的对话语气，像朋友聊天一样
            2. 避免机械的列表格式，使用更自然的段落描述
            3. 保持回复简洁但信息丰富（150-300字）
            4. 使用中文回复，语言通俗易懂
            """, description);
    }
    
    /**
     * 使用Ollama API生成文本
     * @param prompt 提示词
     * @return 生成的文本
     */
    private String generateTextWithOllama(String prompt) {
        try {
            // 使用Ollama的generate API生成文本
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama2");
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of("temperature", 0.7, "max_tokens", 500));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = "http://localhost:11434/api/generate";
            ResponseEntity<Map> response = new RestTemplate().postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("response");
            } else {
                log.error("Ollama API调用失败: {}", response.getStatusCode());
                return generateFallbackRecommendReason();
            }
        } catch (Exception e) {
            log.error("调用Ollama API生成推荐理由失败", e);
            return generateFallbackRecommendReason();
        }
    }
    
    /**
     * 生成备用推荐理由
     * @return 备用推荐理由
     */
    private String generateFallbackRecommendReason() {
        return "基于您的描述，我为您推荐了几部符合您需求的电影。这些电影在剧情、情感表达和制作质量方面都有不错的表现，相信您会喜欢。";
    }
    
    /**
     * 计算推荐置信度
     * @param sortedEntries 排序后的相似度条目
     * @return 置信度
     */
    private BigDecimal calculateConfidence(List<Map.Entry<Long, BigDecimal>> sortedEntries) {
        if (sortedEntries.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 取前5个相似度的平均值作为置信度
        BigDecimal sum = sortedEntries.stream()
                .limit(5)
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int count = Math.min(sortedEntries.size(), 5);
        return sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
    }
}