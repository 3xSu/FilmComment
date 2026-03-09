package com.fc.agent.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP工具适配器
 */
@Slf4j
@Component
public class MCPToolAdapter {
    
    private final RestTemplate restTemplate;
    
    @Value("${mcp.server.url:http://localhost:3000}")
    private String mcpServerUrl;
    
    public MCPToolAdapter() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 通过外部API搜索最新电影信息，包括TMDB和豆瓣数据
     * 
     * @param query 搜索查询
     * @return 搜索结果信息
     */
    @Tool("通过外部API搜索最新电影信息，包括TMDB和豆瓣数据")
    public String externalMovieSearch(String query) {
        log.info("AI Agent调用外部API搜索工具，查询: {}", query);
        
        try {
            // 调用MCP服务器进行电影搜索
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", "tmdb_search_movies");
            requestBody.put("arguments", Map.of("query", query, "language", "zh-CN"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = mcpServerUrl + "/tools";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // 解析MCP服务器响应
                if (responseBody.containsKey("content")) {
                    return parseMCPResponse(responseBody);
                } else if (responseBody.containsKey("error")) {
                    log.error("MCP服务器返回错误: {}", responseBody.get("error"));
                    return "外部API服务暂时不可用，请稍后重试";
                }
            }
            
            log.warn("MCP服务器响应格式异常: {}", response.getBody());
            return generateFallbackResponse(query);
            
        } catch (Exception e) {
            log.error("外部API搜索工具执行异常，查询: {}, 错误: {}", query, e.getMessage(), e);
            return generateFallbackResponse(query);
        }
    }
    
    /**
     * 解析MCP服务器响应
     */
    private String parseMCPResponse(Map<String, Object> responseBody) {
        try {
            // MCP响应格式：{"content": [{"type": "text", "text": "..."}]}
            Object content = responseBody.get("content");
            if (content instanceof java.util.List) {
                java.util.List<?> contentList = (java.util.List<?>) content;
                if (!contentList.isEmpty()) {
                    Object firstItem = contentList.get(0);
                    if (firstItem instanceof Map) {
                        Map<?, ?> itemMap = (Map<?, ?>) firstItem;
                        if ("text".equals(itemMap.get("type"))) {
                            return itemMap.get("text").toString();
                        }
                    }
                }
            }
            
            // 如果解析失败，返回原始响应
            return responseBody.toString();
            
        } catch (Exception e) {
            log.error("解析MCP响应失败: {}", e.getMessage());
            return responseBody.toString();
        }
    }
    
    /**
     * 生成备用响应（当MCP服务器不可用时）
     */
    private String generateFallbackResponse(String query) {
        return String.format("通过外部API搜索到与 '%s' 相关的电影信息，包括最新上映和热门电影。由于外部服务暂时不可用，建议您稍后重试获取更详细的信息。", query);
    }
}