package com.fc.agent.tools;

import com.fc.context.BaseContext;
import com.fc.exception.UserNoViewingDataException;
import com.fc.service.user.AiRecommendService;
import com.fc.vo.movie.ai.MovieRecommendVO;
import com.fc.vo.movie.ai.MovieSimpleVO;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 历史推荐工具类
 * 支持自动获取当前用户上下文和手动指定用户ID两种模式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryRecommendTool {
    
    private final AiRecommendService aiRecommendService;
    
    /**
     * 基于当前用户观影历史生成电影推荐（推荐使用）
     * 自动从线程局部变量获取用户ID，避免传递错误的用户ID
     * 
     * @param count 推荐数量（可选，默认5部）
     * @param genre 指定类型（可选）
     * @param year 指定年份范围（可选）
     * @return 推荐结果信息
     */
    @Tool("基于当前用户观影历史生成电影推荐")
    public String recommendByCurrentUserHistory(Integer count, String genre, String year) {
        // 从线程局部变量获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        
        log.info("AI Agent调用用户上下文感知的历史推荐工具，用户ID: {}, 数量: {}, 类型: {}, 年份: {}", 
                userId, count, genre, year);
        
        return generateRecommendation(userId, count, genre, year);
    }
    
    /**
     * 基于指定用户观影历史生成电影推荐（兼容旧版本）
     * 主要用于管理端或特定场景，普通用户推荐请使用recommendByCurrentUserHistory
     * 
     * @param userId 用户ID
     * @param count 推荐数量（可选，默认5部）
     * @param genre 指定类型（可选）
     * @param year 指定年份范围（可选）
     * @return 推荐结果信息
     */
    @Tool("基于指定用户观影历史生成电影推荐")
    public String recommendByHistory(Long userId, Integer count, String genre, String year) {
        log.info("AI Agent调用历史推荐工具，用户ID: {}, 数量: {}, 类型: {}, 年份: {}", 
                userId, count, genre, year);
        
        return generateRecommendation(userId, count, genre, year);
    }
    
    /**
     * 通用的推荐生成逻辑
     */
    private String generateRecommendation(Long userId, Integer count, String genre, String year) {
        try {
            // 参数验证和默认值设置
            if (count == null || count <= 0) {
                count = 5; // 默认推荐5部电影
            }
            if (count > 20) {
                count = 20; // 限制最大推荐数量
            }
            
            MovieRecommendVO result = aiRecommendService.generateMovieRecommend(userId);
            
            if (result == null || result.getRecommendMovies() == null || result.getRecommendMovies().isEmpty()) {
                StringBuilder message = new StringBuilder("未找到基于观影历史的推荐电影");
                if (genre != null) {
                    message.append("，指定类型: ").append(genre);
                }
                if (year != null) {
                    message.append("，年份范围: ").append(year);
                }
                message.append("。建议先观看一些电影来丰富观影历史");
                return message.toString();
            }
            
            // 过滤结果（如果指定了类型或年份）
            List<MovieSimpleVO> filteredMovies = new ArrayList<>();
            if (genre != null || year != null) {
                // 这里需要根据实际业务逻辑实现过滤
                // 暂时返回所有结果，后续可以集成更复杂的过滤逻辑
                filteredMovies = result.getRecommendMovies();
            } else {
                // 限制返回数量
                filteredMovies = result.getRecommendMovies().stream()
                        .limit(count)
                        .collect(Collectors.toList());
            }
            
            StringBuilder message = new StringBuilder("基于观影历史，为您推荐了 " + filteredMovies.size() + " 部电影");
            if (genre != null) {
                message.append("，指定类型: ").append(genre);
            }
            if (year != null) {
                message.append("，年份范围: ").append(year);
            }
            message.append("。推荐理由: ").append(result.getRecommendReason());
            
            return message.toString();
        } catch (UserNoViewingDataException e) {
            log.warn("用户无观影数据，用户ID: {}, 错误: {}", userId, e.getMessage());
            StringBuilder message = new StringBuilder("您还没有观影历史数据");
            if (genre != null) {
                message.append("，无法基于指定类型 '").append(genre).append("' 进行推荐");
            }
            if (year != null) {
                message.append("，年份范围: ").append(year);
            }
            message.append("。建议您先观看一些电影，系统会根据您的观影偏好为您推荐更精准的内容");
            return message.toString();
        } catch (Exception e) {
            log.error("历史推荐工具执行异常，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return "生成推荐时发生错误，请稍后重试";
        }
    }
    
    /**
     * 获取当前用户观影历史统计信息
     * 
     * @return 观影历史统计信息
     */
    @Tool("获取当前用户观影历史统计信息")
    public String getCurrentUserHistoryStats() {
        // 从线程局部变量获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        
        log.info("AI Agent调用用户上下文感知的观影历史统计工具，用户ID: {}", userId);
        
        try {
            // 这里可以调用统计服务获取用户观影历史数据
            // 暂时返回示例数据
            return "您已观看过50部电影，最喜欢的类型是科幻和动作片，平均评分8.2分";
        } catch (Exception e) {
            log.error("用户上下文感知的观影历史统计工具执行异常，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return "获取观影统计时发生错误，请稍后重试";
        }
    }
    
    /**
     * 获取指定用户观影历史统计信息（兼容旧版本）
     * 
     * @param userId 用户ID
     * @return 观影历史统计信息
     */
    @Tool("获取指定用户观影历史统计信息")
    public String getHistoryStats(Long userId) {
        log.info("AI Agent调用观影历史统计工具，用户ID: {}", userId);
        
        try {
            // 这里可以调用统计服务获取用户观影历史数据
            // 暂时返回示例数据
            return "该用户已观看过50部电影，最喜欢的类型是科幻和动作片，平均评分8.2分";
        } catch (Exception e) {
            log.error("观影历史统计工具执行异常，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return "获取观影统计时发生错误，请稍后重试";
        }
    }
}