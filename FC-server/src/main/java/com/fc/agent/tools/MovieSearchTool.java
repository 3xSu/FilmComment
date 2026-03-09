package com.fc.agent.tools;

import com.fc.dto.movie.admin.SearchMovieDTO;
import com.fc.result.PageResult;
import com.fc.service.api.MoviePublicService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 电影搜索工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieSearchTool {
    
    private final MoviePublicService moviePublicService;
    
    /**
     * 根据关键词搜索电影信息，支持按时长、年份、排序方式筛选
     * 
     * @param keyword 搜索关键词
     * @param minDuration 最小时长（分钟，可选）
     * @param maxDuration 最大时长（分钟，可选）
     * @param releaseYear 上映年份（可选）
     * @param sortType 排序方式：1-相关性，2-评分，3-上映时间（可选）
     * @return 搜索结果信息
     */
    @Tool("根据关键词搜索电影信息，支持按时长、年份、排序方式筛选")
    public String searchMovies(String keyword, Integer minDuration, Integer maxDuration, Integer releaseYear, Integer sortType) {
        log.info("AI Agent调用电影搜索工具，关键词: {}, 最小时长: {}, 最大时长: {}, 上映年份: {}, 排序方式: {}", 
                keyword, minDuration, maxDuration, releaseYear, sortType);
        
        try {
            // 参数验证
            if (keyword == null || keyword.trim().isEmpty()) {
                return "搜索关键词不能为空";
            }
            
            SearchMovieDTO searchDTO = new SearchMovieDTO();
            searchDTO.setKeyword(keyword.trim());
            searchDTO.setPage(0);
            searchDTO.setSize(10);
            
            // 设置可选参数
            if (minDuration != null && minDuration > 0) {
                searchDTO.setMinDuration(minDuration);
            }
            if (maxDuration != null && maxDuration > 0) {
                searchDTO.setMaxDuration(maxDuration);
            }
            if (releaseYear != null && releaseYear > 1900 && releaseYear <= 2030) {
                searchDTO.setReleaseYear(releaseYear);
            }
            if (sortType != null && sortType >= 1 && sortType <= 3) {
                searchDTO.setSortType(sortType);
            }
            
            PageResult result = moviePublicService.searchMovies(searchDTO);
            
            if (result.getTotal() == 0) {
                StringBuilder message = new StringBuilder("未找到与关键词 '" + keyword + "' 相关的电影信息");
                if (minDuration != null) {
                    message.append("，最小时长: ").append(minDuration).append("分钟");
                }
                if (maxDuration != null) {
                    message.append("，最大时长: ").append(maxDuration).append("分钟");
                }
                if (releaseYear != null) {
                    message.append("，上映年份: ").append(releaseYear);
                }
                return message.toString();
            }
            
            StringBuilder message = new StringBuilder("找到 " + result.getTotal() + " 部与 '" + keyword + "' 相关的电影");
            if (minDuration != null) {
                message.append("，最小时长: ").append(minDuration).append("分钟");
            }
            if (maxDuration != null) {
                message.append("，最大时长: ").append(maxDuration).append("分钟");
            }
            if (releaseYear != null) {
                message.append("，上映年份: ").append(releaseYear);
            }
            if (sortType != null) {
                String sortDesc = getSortDescription(sortType);
                message.append("，排序方式: ").append(sortDesc);
            }
            message.append("。详细信息请查看搜索结果");
            
            return message.toString();
        } catch (Exception e) {
            log.error("电影搜索工具执行异常，关键词: {}, 错误: {}", keyword, e.getMessage(), e);
            return "搜索电影时发生错误，请稍后重试";
        }
    }
    
    /**
     * 获取排序方式描述
     */
    private String getSortDescription(Integer sortType) {
        switch (sortType) {
            case 1: return "相关性排序";
            case 2: return "评分排序";
            case 3: return "上映时间排序";
            default: return "默认排序";
        }
    }
    
    /**
     * 高级电影搜索，支持更多筛选条件
     * 
     * @param searchParams 搜索参数Map
     * @return 搜索结果信息
     */
    @Tool("高级电影搜索，支持更多筛选条件")
    public String advancedSearch(Map<String, Object> searchParams) {
        log.info("AI Agent调用高级电影搜索工具，参数: {}", searchParams);
        
        try {
            SearchMovieDTO searchDTO = new SearchMovieDTO();
            searchDTO.setPage(0);
            searchDTO.setSize(15);
            
            // 解析参数
            if (searchParams.containsKey("keyword")) {
                String keyword = searchParams.get("keyword").toString();
                if (!keyword.trim().isEmpty()) {
                    searchDTO.setKeyword(keyword.trim());
                }
            }
            if (searchParams.containsKey("minDuration")) {
                String minDurationStr = searchParams.get("minDuration").toString();
                if (!minDurationStr.trim().isEmpty()) {
                    try {
                        searchDTO.setMinDuration(Integer.parseInt(minDurationStr.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("minDuration参数格式错误: {}", minDurationStr);
                    }
                }
            }
            if (searchParams.containsKey("maxDuration")) {
                String maxDurationStr = searchParams.get("maxDuration").toString();
                if (!maxDurationStr.trim().isEmpty()) {
                    try {
                        searchDTO.setMaxDuration(Integer.parseInt(maxDurationStr.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("maxDuration参数格式错误: {}", maxDurationStr);
                    }
                }
            }
            if (searchParams.containsKey("releaseYear")) {
                String releaseYearStr = searchParams.get("releaseYear").toString();
                if (!releaseYearStr.trim().isEmpty()) {
                    try {
                        searchDTO.setReleaseYear(Integer.parseInt(releaseYearStr.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("releaseYear参数格式错误: {}", releaseYearStr);
                    }
                }
            }
            if (searchParams.containsKey("sortType")) {
                String sortTypeStr = searchParams.get("sortType").toString();
                if (!sortTypeStr.trim().isEmpty()) {
                    try {
                        searchDTO.setSortType(Integer.parseInt(sortTypeStr.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("sortType参数格式错误: {}", sortTypeStr);
                    }
                }
            }
            
            PageResult result = moviePublicService.searchMovies(searchDTO);
            
            if (result.getTotal() == 0) {
                StringBuilder message = new StringBuilder("未找到符合搜索条件的电影信息");
                if (searchParams.containsKey("keyword")) {
                    message.append("，关键词: ").append(searchParams.get("keyword"));
                }
                if (searchParams.containsKey("releaseYear")) {
                    message.append("，上映年份: ").append(searchParams.get("releaseYear"));
                }
                return message.toString();
            }
            
            StringBuilder message = new StringBuilder("找到 " + result.getTotal() + " 部符合搜索条件的电影");
            if (searchParams.containsKey("keyword")) {
                message.append("，关键词: ").append(searchParams.get("keyword"));
            }
            if (searchParams.containsKey("minDuration")) {
                message.append("，最小时长: ").append(searchParams.get("minDuration")).append("分钟");
            }
            if (searchParams.containsKey("maxDuration")) {
                message.append("，最大时长: ").append(searchParams.get("maxDuration")).append("分钟");
            }
            if (searchParams.containsKey("releaseYear")) {
                message.append("，上映年份: ").append(searchParams.get("releaseYear"));
            }
            if (searchParams.containsKey("sortType")) {
                String sortDesc = getSortDescription(Integer.parseInt(searchParams.get("sortType").toString()));
                message.append("，排序方式: ").append(sortDesc);
            }
            
            return message.toString();
        } catch (Exception e) {
            log.error("高级电影搜索工具执行异常，参数: {}, 错误: {}", searchParams, e.getMessage(), e);
            return "高级搜索时发生错误，请稍后重试";
        }
    }
}