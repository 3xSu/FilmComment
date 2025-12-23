package com.fc.service.impl.user;

import com.fc.constant.MessageConstant;
import com.fc.context.BaseContext;
import com.fc.dto.ai.MovieCommentSummaryDTO;
import com.fc.entity.AiRecord;
import com.fc.entity.Movie;
import com.fc.enums.PostTypeEnum;
import com.fc.exception.MovieNotFoundException;
import com.fc.mapper.admin.MovieAdminMapper;
import com.fc.mapper.user.AiRecordMapper;
import com.fc.mapper.user.PostUserMapper;
import com.fc.service.user.AiService;
import com.fc.utils.OllamaUtil;
import com.fc.vo.ai.MovieCommentSummaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private AiRecordMapper aiRecordMapper;

    @Autowired
    private MovieAdminMapper movieAdminMapper;

    @Autowired
    private PostUserMapper postUserMapper;

    @Autowired
    private OllamaUtil ollamaUtil;

    @Value("${ai.comment-analysis.sample-size:50}")
    private Integer sampleSize;

    @Value("${ai.summary.update-threshold:3}")
    private Integer defaultUpdateThreshold;

    @Value("${ai.summary.cache-minutes:30}")
    private Integer cacheMinutes;

    /**
     * 生成电影评论AI总结
     * @param summaryDTO
     * @return
     */
    @Override
    @Transactional
    public MovieCommentSummaryVO generateMovieCommentSummary(MovieCommentSummaryDTO summaryDTO) {
        Long userId = BaseContext.getCurrentId();
        Long movieId = summaryDTO.getMovieId();
        Integer postType = summaryDTO.getPostType();
        Integer updateThreshold = summaryDTO.getUpdateThreshold() != null ?
                summaryDTO.getUpdateThreshold() : defaultUpdateThreshold;

        log.info("智能生成电影评论AI总结: userId={}, movieId={}, postType={}, threshold={}",
                userId, movieId, postType, updateThreshold);

        // 1. 校验postType，仅允许1、2类型，其他类型直接返回提示
        if (postType != null && (postType == 3 || postType == 4)) {
            log.info("二创区帖子不生成AI总结: postType={}", postType);
            Movie movie = movieAdminMapper.getByMovieId(movieId);
            if (movie == null) {
                throw new MovieNotFoundException(MessageConstant.MOVIE_NOT_FOUND);
            }
            return MovieCommentSummaryVO.builder()
                    .movieId(movieId)
                    .movieTitle(movie.getTitle())
                    .createTime(LocalDateTime.now())
                    .build();
        }

        // 2. 验证电影是否存在
        Movie movie = movieAdminMapper.getByMovieId(movieId);
        if (movie == null) {
            throw new MovieNotFoundException(MessageConstant.MOVIE_NOT_FOUND);
        }

        // 3. 获取当前电影帖子数量
        Integer currentPostCount = getCurrentPostCount(movieId, postType);

        // 4. 检查帖子数量是否足够生成AI总结
        if (currentPostCount == 0 || currentPostCount < updateThreshold) {
            log.info("帖子数量不足，无法生成AI总结: movieId={}, postType={}, currentPostCount={}, threshold={}",
                    movieId, postType, currentPostCount, updateThreshold);
            return buildInsufficientPostsSummary(movie, currentPostCount, updateThreshold, postType);
        }

        // 5. 检查强制刷新标志
        if (Boolean.TRUE.equals(summaryDTO.getForceRefresh())) {
            log.info("强制刷新AI总结: movieId={}, postType={}", movieId, postType);
            return generateNewSummary(userId, movieId, movie, summaryDTO, currentPostCount, updateThreshold, postType);
        }

        // 6. 检查是否有可用的现有总结
        AiRecord cachedSummary = getAvailableSummary(movieId, currentPostCount, updateThreshold, postType);
        if (cachedSummary != null) {
            log.info("使用缓存的AI总结: recordId={}, postType={}, 当前帖子数={}, 缓存帖子数={}",
                    cachedSummary.getRecordId(), postType, currentPostCount, cachedSummary.getPostCount());
            return buildSummaryVO(cachedSummary, movie);
        }

        // 7. 需要重新生成总结
        return generateNewSummary(userId, movieId, movie, summaryDTO, currentPostCount, updateThreshold, postType);
    }

    /**
     * 获取当前电影指定帖子类型的帖子数量
     * @param movieId
     * @param postType
     * @return
     */
    private Integer getCurrentPostCount(Long movieId, Integer postType) {
        try {
            if (postType != null) {
                return postUserMapper.countValidPostsByMovieIdAndPostType(movieId, postType);
            } else {
                return postUserMapper.countValidPostsByMovieId(movieId);
            }
        } catch (Exception e) {
            log.error("获取电影帖子数量失败: movieId={}, postType={}", movieId, postType, e);
            return 0;
        }
    }

    /**
     * 获取可用的AI总结（检查缓存条件）
     * @param movieId
     * @param currentPostCount
     * @param updateThreshold
     * @return
     */
    private AiRecord getAvailableSummary(Long movieId, Integer currentPostCount, Integer updateThreshold, Integer postType) {
        try {
            // 查询最近的AI总结记录
            AiRecord latestSummary = aiRecordMapper.getLatestMovieSummaryByPostType(movieId, postType);
            if (latestSummary == null) {
                return null;
            }

            // 检查时间窗口（30分钟内直接使用）
            if (isWithinCacheTime(latestSummary.getCreateTime())) {
                log.info("使用时间窗口内的AI总结");
                return latestSummary;
            }

            // 如果当前帖子数不是阈值的倍数，且与上次生成时的帖子数相同，则使用缓存
            boolean isThresholdMultiple = currentPostCount % updateThreshold == 0;
            boolean postCountUnchanged = currentPostCount.equals(latestSummary.getPostCount());

            if (!isThresholdMultiple || postCountUnchanged) {
                log.info("帖子总数{}不是阈值{}的倍数，使用现有总结: postType={}",
                        currentPostCount, updateThreshold, postType);
                return latestSummary;
            }

            return null;

        } catch (Exception e) {
            log.error("获取可用总结失败", e);
            return null;
        }
    }

    /**
     * 检查是否在时间窗口内
     * @param createTime
     * @return
     */
    private boolean isWithinCacheTime(LocalDateTime createTime) {
        return createTime.isAfter(LocalDateTime.now().minusMinutes(cacheMinutes));
    }

    /**
     * 生成新的AI总结
     * @param userId
     * @param movieId
     * @param movie
     * @param summaryDTO
     * @param currentPostCount
     * @param updateThreshold
     * @return
     */
    private MovieCommentSummaryVO generateNewSummary(Long userId, Long movieId, Movie movie,
                                                     MovieCommentSummaryDTO summaryDTO,
                                                     Integer currentPostCount, Integer updateThreshold,
                                                     Integer postType) {
        log.info("生成新的AI总结: movieId={}, postType={}, 当前帖子数={}",
                movieId, postType, currentPostCount);

        // 检查帖子数是否是阈值的倍数
        if (currentPostCount % updateThreshold != 0) {
            log.info("帖子总数{}不是阈值{}的倍数，不生成新总结: postType={}",
                    currentPostCount, updateThreshold, postType);

            // 返回最近的总结（如果有），否则返回提示
            AiRecord latestSummary = aiRecordMapper.getLatestMovieSummaryByPostType(movieId, postType);
            if (latestSummary != null) {
                return buildSummaryVO(latestSummary, movie);
            } else {
                return buildInsufficientPostsSummary(movie, currentPostCount, updateThreshold, postType);
            }
        }

        // 1. 获取评论样本进行分析
        List<String> commentSamples = getCommentSamplesForAnalysis(movieId, sampleSize, postType);
        if (commentSamples.isEmpty()) {
            return buildEmptySummary(movie, currentPostCount, postType);
        }

        String samplesText = String.join("\n", commentSamples);

        // 2. 生成AI总结
        String aiSummary;
        try {
            aiSummary = ollamaUtil.generateMovieCommentSummary(
                    movie.getTitle(), samplesText,
                    summaryDTO.getSummaryStyle(), summaryDTO.getMaxLength());
        } catch (Exception e) {
            log.error("AI总结生成失败，使用备用总结: postType={}", postType, e);
            aiSummary = generateFallbackSummary(movie.getTitle(), commentSamples.size(), postType);
        }


        // 3. 保存新的AI记录（更新版本号）
        AiRecord latestSummary = aiRecordMapper.getLatestMovieSummaryByPostType(movieId, postType);
        Integer newVersion = latestSummary != null ? latestSummary.getVersion() + 1 : 1;

        AiRecord aiRecord = AiRecord.builder()
                .userId(userId)
                .movieId(movieId)
                .type(2) // 影评总结
                .summaryType(1) // 电影评论总结
                .content(aiSummary)
                .postCount(currentPostCount)
                .version(newVersion)
                .threshold(updateThreshold)
                .postType(postType)
                .createTime(LocalDateTime.now())
                .build();

        aiRecordMapper.insert(aiRecord);

        log.info("新的AI总结生成成功: recordId={}, version={}, postCount={}, postType={}",
                aiRecord.getRecordId(), newVersion, currentPostCount, summaryDTO.getPostType());

        return buildSummaryVO(aiRecord, movie);
    }


    /**
     * 获取电影的评论样本用于AI分析
     * @param movieId
     * @param sampleSize
     * @param postType 帖子类型筛选
     * @return
     */
    private List<String> getCommentSamplesForAnalysis(Long movieId, Integer sampleSize, Integer postType) {
        try {
            // 根据帖子类型构建查询条件
            return postUserMapper.getCommentSamplesByPostType(movieId, sampleSize, postType);

        } catch (Exception e) {
            log.error("获取评论样本失败: movieId={}", movieId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成备用总结
     * @param movieTitle
     * @param sampleSize
     * @param postType
     * @return
     */
    private String generateFallbackSummary(String movieTitle, int sampleSize, Integer postType) {
        String typeDesc = getPostTypeDescription(postType);
        return String.format("基于%d条%s用户评论，电影《%s》的讨论主要集中在剧情、演技和视觉效果等方面。观众普遍认为这是一部值得观看的作品。",
                sampleSize, typeDesc, movieTitle);
    }

    /**
     * 获取帖子类型描述
     * @param postType
     * @return
     */
    private String getPostTypeDescription(Integer postType) {
        if (postType == null) return "";
        try {
            PostTypeEnum type = PostTypeEnum.getByCode(postType);
            return type.getDesc(); // 需要在枚举类中添加getDesc()方法
        } catch (IllegalArgumentException e) {
            log.warn("无效的postType: {}", postType);
            return "";
        }
    }

    /**
     * 构建帖子数量不足的总结
     * @param movie
     * @param postCount
     * @param threshold
     * @return
     */
    private MovieCommentSummaryVO buildInsufficientPostsSummary(Movie movie, Integer postCount,
                                                                Integer threshold, Integer postType) {
        String message;
        if (postCount == 0) {
            message = String.format("当前还没有关于这部电影的%s帖子，快来发表第一个帖子吧！",
                    getPostTypeDescription(postType));
        } else {
            message = String.format("当前%s帖子数量(%d条)较少，快来发表更多帖子参与讨论吧！",
                    getPostTypeDescription(postType), postCount);
        }

        // 不存入数据库，直接构建VO返回
        return MovieCommentSummaryVO.builder()
                .movieId(movie.getMovieId())
                .movieTitle(movie.getTitle())
                .summaryContent(message)
                .summaryStyle(1)
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 构建空数据总结
     * @param movie
     * @param postCount
     * @return
     */
    private MovieCommentSummaryVO buildEmptySummary(Movie movie, Integer postCount, Integer postType) {
        String emptySummary = String.format(
                "电影《%s》的%s帖子目前还没有足够的评论数据来生成AI总结。当前共有%d条帖子，请等待更多用户参与讨论。",
                movie.getTitle(), getPostTypeDescription(postType), postCount
        );

        // 不存入数据库，直接构建VO返回
        return MovieCommentSummaryVO.builder()
                .movieId(movie.getMovieId())
                .movieTitle(movie.getTitle())
                .summaryContent(emptySummary)
                .summaryStyle(1)
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 构建SummaryVO对象
     * @param record
     * @param movie
     * @return
     */
    private MovieCommentSummaryVO buildSummaryVO(AiRecord record, Movie movie) {

        MovieCommentSummaryVO.SummaryStats stats = calculateSummaryStats(
                record.getMovieId(), record.getPostType(), record.getPostCount());

        return MovieCommentSummaryVO.builder()
                .recordId(record.getRecordId())
                .movieId(record.getMovieId())
                .movieTitle(movie != null ? movie.getTitle() : "未知电影")
                .summaryContent(record.getContent())
                .summaryStyle(1) // 默认为简洁版
                .createTime(record.getCreateTime())
                .stats(stats)
                .build();
    }

    /**
     * 计算 AI 总结相关的统计数据
     * @param movieId
     * @param postType
     * @param sampleSize
     * @return
     */
    private MovieCommentSummaryVO.SummaryStats calculateSummaryStats(Long movieId, Integer postType, int sampleSize) {
        try {
            // 获取各种类型的评论数量
            Integer totalComments = getCurrentPostCount(movieId, postType);
            Integer noSpoilerComments = (postType == null || postType == 1)
                    ? getCurrentPostCount(movieId, 1)
                    : 0;
            Integer spoilerComments = (postType == null || postType == 2)
                    ? getCurrentPostCount(movieId, 2)
                    : 0;

            // 以后添加情感分析来计算比例
//            Double positiveRatio = calculatePositiveRatio(movieId, postType);
//            Double negativeRatio = calculateNegativeRatio(movieId, postType);
//            Double neutralRatio = 1.0 - positiveRatio - negativeRatio;

            return MovieCommentSummaryVO.SummaryStats.builder()
                    .totalComments(totalComments)
                    .noSpoilerComments(noSpoilerComments)
                    .spoilerComments(spoilerComments)
                    .positiveRatio(0.0)
                    .negativeRatio(0.0)
                    .neutralRatio(0.0)
                    .build();
        } catch (Exception e) {
            log.error("计算统计信息失败", e);
            return null;
        }
    }

    /**
     * 检查AI服务可用性
     * @return
     */
    @Override
    public boolean checkAiServiceAvailability() {
        try {
            // 实际测试Ollama服务连通性
            return ollamaUtil.testConnection();
        } catch (Exception e) {
            log.error("AI服务检查失败", e);
            return false;
        }
    }

}