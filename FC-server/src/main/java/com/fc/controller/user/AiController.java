package com.fc.controller.user;

import com.fc.context.BaseContext;
import com.fc.dto.ai.MovieCommentSummaryDTO;
import com.fc.result.Result;
import com.fc.service.user.AiService;
import com.fc.vo.ai.MovieCommentSummaryVO;
import com.fc.vo.ai.MovieSummaryVersionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/ai")
@Tag(name = "用户AI功能接口")
@Slf4j
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * 根据电影ID获取AI总结信息
     * @param movieId
     * @param forceRefresh
     * @return
     */
    @GetMapping("/movie/{movieId}/summary")
    @Operation(summary = "获取电影的AI总结信息")
    public Result<MovieCommentSummaryVO> getMovieSummary(
            @PathVariable Long movieId,
            @RequestParam(value = "postType", required = true) Integer postType,
            @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") Boolean forceRefresh) {

        // 参数验证
        if (postType < 1 || postType > 4) {
            return Result.error("帖子类型参数错误，可选值：1-无剧透普通，2-有剧透深度，3-二创无剧透，4-二创有剧透");
        }

        log.info("获取电影AI总结信息: movieId={}, postType={}, forceRefresh={}", movieId, postType, forceRefresh);

        MovieCommentSummaryDTO summaryDTO = new MovieCommentSummaryDTO();
        summaryDTO.setMovieId(movieId);
        summaryDTO.setPostType(postType);
        summaryDTO.setForceRefresh(forceRefresh);

        MovieCommentSummaryVO summaryVO = aiService.generateMovieCommentSummary(summaryDTO);
        return Result.success(summaryVO);
    }

    /**
     * 检查AI服务状态
     * @return
     */
    @GetMapping("/status")
    @Operation(summary = "检查AI服务状态")
    public Result<Boolean> checkAiServiceStatus() {
        boolean available = aiService.checkAiServiceAvailability();
        log.info("检查AI服务状态: {}", available ? "可用" : "不可用");
        return Result.success(available);
    }
}