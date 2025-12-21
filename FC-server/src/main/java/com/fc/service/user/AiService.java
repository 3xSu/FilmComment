package com.fc.service.user;

import com.fc.dto.ai.MovieCommentSummaryDTO;
import com.fc.vo.ai.MovieCommentSummaryVO;
import com.fc.vo.ai.MovieSummaryStatsVO;

import java.util.Map;

public interface AiService {

    /**
     * 生成电影评论AI总结
     *
     * @param summaryDTO
     * @return
     */
    MovieCommentSummaryVO generateMovieCommentSummary(MovieCommentSummaryDTO summaryDTO);

    /**
     * 检查AI服务可用性
     *
     * @return
     */
    boolean checkAiServiceAvailability();

}