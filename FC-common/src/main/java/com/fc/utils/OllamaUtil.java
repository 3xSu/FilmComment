package com.fc.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OllamaUtil {

    @Value("${ollama.api.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ollama.api.model:llama2}")
    private String model;

    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private PromptManager promptManager;

    private final RestTemplate restTemplate;

    public OllamaUtil() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 生成电影评论AI总结
     */
    public String generateMovieCommentSummary(String movieTitle, String commentSamples, Integer summaryStyle, Integer maxLength) {
        // 创建分布式锁，锁的key包含电影标题，确保同一电影串行处理
        String lockKey = String.format("lock:movie:summary:%s", movieTitle.hashCode());
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;
        try {
            // 尝试获取锁，最多等待3秒，锁持有时间90秒
            isLocked = lock.tryLock(3, 90, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("获取分布式锁失败，电影《{}》的AI总结生成任务已被其他实例处理", movieTitle);
                // 可以返回一个提示，或者抛出特定异常
                return generateFallbackSummary(movieTitle, summaryStyle);
            }

            log.info("成功获取分布式锁，开始为电影《{}》生成AI总结", movieTitle);
            return doGenerateMovieCommentSummary(movieTitle, commentSamples, summaryStyle, maxLength);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁时被中断，电影《{}》", movieTitle, e);
            return generateFallbackSummary(movieTitle, summaryStyle);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("已释放分布式锁，电影《{}》的AI总结生成完成", movieTitle);
            }
        }
    }

    /**
     * 实际的AI总结生成逻辑（被锁保护的部分）
     */
    private String doGenerateMovieCommentSummary(String movieTitle, String commentSamples, Integer summaryStyle, Integer maxLength) {
        try {
            String prompt = buildMovieSummaryPrompt(movieTitle, commentSamples, summaryStyle, maxLength);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of("temperature", 0.7, "max_tokens", maxLength));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/api/generate";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("response");
            } else {
                log.error("Ollama API调用失败: {}", response.getStatusCode());
                return generateFallbackSummary(movieTitle, summaryStyle);
            }
        } catch (Exception e) {
            log.error("调用Ollama API生成电影评论总结失败", e);
            return generateFallbackSummary(movieTitle, summaryStyle);
        }
    }

    /**
     * 构建AI提示词
     */
    private String buildMovieSummaryPrompt(String movieTitle, String commentSamples, Integer summaryStyle, Integer maxLength) {
        // 使用注入的PromptManager构建提示词
        return promptManager.buildMovieCommentSummaryPrompt(movieTitle, commentSamples, summaryStyle, maxLength);
    }

    /**
     * 生成备用总结（当AI服务不可用时）
     */
    private String generateFallbackSummary(String movieTitle, Integer summaryStyle) {
        String style = summaryStyle == 1 ? "简洁版" : "详细版";
        return String.format(
                "【%s】由于当前AI服务暂时不可用，以下是电影《%s》的默认总结模板：\n\n" +
                        "这部电影在观众中引起了广泛讨论。建议您稍后重试获取更准确的AI分析总结。",
                style, movieTitle
        );
    }

    /**
     * 检查Ollama服务是否可用
     */
    public boolean testConnection() {
        try {
            String url = baseUrl + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("Ollama服务不可用: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成文本的向量表示
     * @param text 输入文本
     * @return 向量表示
     */
    public List<Double> generateVector(String text) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", text);
            requestBody.put("stream", false);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 使用 Ollama Embeddings API 端点
            String url = baseUrl + "/api/embeddings";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 解析真实的 Ollama API 响应获取向量
                Map<String, Object> responseBody = response.getBody();
                
                // Ollama Embeddings API 返回格式：
                // {
                //   "embeddings": [[0.1, 0.2, 0.3, ...]]
                // }
                if (responseBody.containsKey("embeddings")) {
                    List<List<Double>> embeddings = (List<List<Double>>) responseBody.get("embeddings");
                    if (embeddings != null && !embeddings.isEmpty()) {
                        return embeddings.get(0); // 返回第一个嵌入向量
                    }
                }
                
                // 如果响应格式不符合预期，记录警告并使用备用方案
                log.warn("Ollama API 响应格式不符合预期: {}", responseBody);
                return generateRandomVector();
            } else {
                log.error("Ollama API调用失败: {}", response.getStatusCode());
                // 返回随机向量作为备用
                return generateRandomVector();
            }
        } catch (Exception e) {
            log.error("调用Ollama API生成向量失败", e);
            // 返回随机向量作为备用
            return generateRandomVector();
        }
    }
    
    /**
     * 生成随机向量作为备用
     * @return 随机向量
     */
    private List<Double> generateRandomVector() {
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            vector.add(Math.random());
        }
        return vector;
    }
    
    /**
     * 生成文本内容
     * @param prompt 输入提示词
     * @return 生成的文本内容
     */
    public String generateText(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of("temperature", 0.7, "max_tokens", 500));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/api/generate";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("response");
            } else {
                log.error("Ollama API调用失败: {}", response.getStatusCode());
                return generateFallbackText(prompt);
            }
        } catch (Exception e) {
            log.error("调用Ollama API生成文本失败", e);
            return generateFallbackText(prompt);
        }
    }
    
    /**
     * 生成备用文本（当AI服务不可用时）
     * @param prompt 原始提示词
     * @return 备用文本
     */
    private String generateFallbackText(String prompt) {
        log.warn("AI服务不可用，使用备用文本生成");
        return "由于当前AI服务暂时不可用，无法生成智能推荐理由。请稍后重试。";
    }
}