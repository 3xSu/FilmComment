package com.fc.controller.user;

import com.fc.context.BaseContext;
import com.fc.result.Result;
import com.fc.service.user.AiRecommendService;
import com.fc.vo.movie.ai.MovieRecommendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI电影推荐控制器
 */
@RestController
@RequestMapping("/user/ai/recommend")
@Tag(name = "AI电影推荐接口")
@Slf4j
public class AiRecommendController {
    
    @Autowired
    private AiRecommendService aiRecommendService;
    
    /**
     * 基于用户观影历史推荐电影
     * @return 推荐结果
     */
    @PostMapping("/history")
    @Operation(summary = "基于观影历史推荐电影")
    public Result<MovieRecommendVO> recommendByHistory() {
        Long userId = BaseContext.getCurrentId();
        log.info("基于观影历史推荐：userId={}", userId);
        
        MovieRecommendVO recommendVO = aiRecommendService.generateMovieRecommend(userId);
        
        log.info("推荐完成：userId={}, 推荐电影数量={}", userId, recommendVO.getRecommendMovies().size());
        return Result.success(recommendVO);
    }
    
    /**
     * 基于自然语言描述推荐电影
     * @param description 电影描述
     * @return 推荐结果
     */
    @PostMapping("/nlp")
    @Operation(summary = "基于自然语言描述推荐电影")
    public Result<MovieRecommendVO> recommendByNlp(@RequestParam String description) {
        Long userId = BaseContext.getCurrentId();
        log.info("基于自然语言推荐：userId={}, 描述长度={}", userId, description.length());
        
        MovieRecommendVO recommendVO = aiRecommendService.recommendByDescription(userId, description);
        
        log.info("推荐完成：userId={}, 推荐电影数量={}", userId, recommendVO.getRecommendMovies().size());
        return Result.success(recommendVO);
    }
}
