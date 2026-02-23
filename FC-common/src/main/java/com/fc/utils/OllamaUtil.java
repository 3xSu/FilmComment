package com.fc.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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
        String style = summaryStyle == 1 ? "简洁明了，突出重点" : "详细全面，包含具体例子";

        return String.format(
                "你是一个专业的电影评论分析师。请根据以下用户对电影《%s》的评论，生成一个%s的总结报告。\n\n" +
                        "要求：\n" +
                        "1. 分析评论中的主要观点和情感倾向\n" +
                        "2. 总结突出的优点和主要槽点\n" +
                        "3. 识别常见的关键词和主题\n" +
                        "4. 评估总体评价倾向\n" +
                        "5. 总结长度控制在%d字以内\n\n" +
                        "用户评论样本：\n%s\n\n" +
                        "请用中文回复，结构清晰，直接给出总结内容：",
                movieTitle, style, maxLength, commentSamples
        );
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
}