package com.fc.controller.user;

import com.fc.context.BaseContext;
import com.fc.dto.movie.user.RatingSubmitDTO;
import com.fc.dto.movie.user.UserMovieRelationDTO;
import com.fc.result.Result;
import com.fc.service.user.MovieUserService;
import com.fc.vo.movie.user.RatingVO;
import com.fc.vo.movie.user.UserMovieRelationVO;
import com.fc.vo.movie.user.UserRelationStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/movie/relation")
@Tag(name = "用户电影关系接口")
@Slf4j
public class MovieUserController {

    @Autowired
    private MovieUserService movieUserService;

    /**
     * 标记观影状态，null=未看，1=想看，2=已看
     * @param relationDTO
     * @return
     */
    @PostMapping("/mark")
    @Operation(summary = "标记观影状态")
    public Result<UserMovieRelationVO> markMovieRelation(@RequestBody @Validated UserMovieRelationDTO relationDTO) {
        log.info("标记观影状态: {}", relationDTO);
        Long userId = BaseContext.getCurrentId();
        UserMovieRelationVO result = movieUserService.markMovieRelation(userId, relationDTO);
        return Result.success(result);
    }

    /**
     * 取消标记（变回null）
     * @param movieId
     * @return
     */
    @DeleteMapping("/unmark/{movieId}")
    @Operation(summary = "取消标记")
    public Result<String> unmarkMovieRelation(@PathVariable Long movieId) {
        log.info("取消标记: movieId={}", movieId);
        Long userId = BaseContext.getCurrentId();
        movieUserService.unmarkMovieRelation(userId, movieId);
        return Result.success("取消标记成功");
    }

    /**
     * 用户与电影关系获取详情
     * @param relationType
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户的电影关系列表")
    public Result<List<UserMovieRelationVO>> getUserMovieRelations(
            @RequestParam(required = false) Integer relationType) {
        Long userId = BaseContext.getCurrentId();
        List<UserMovieRelationVO> relations = movieUserService
                .getUserMovieRelations(userId, relationType);
        return Result.success(relations);
    }

    /**
     * 用户个人中心已看过电影计数
     * @return
     */
    @GetMapping("/stats")
    @Operation(summary = "获取用户的关系统计")
    public Result<UserRelationStatsVO> getUserRelationStats() {
        Long userId = BaseContext.getCurrentId();
        UserRelationStatsVO stats = movieUserService.getUserRelationStats(userId);
        return Result.success(stats);
    }

    /**
     * 用户点进电影详情页时显示关系
     * @param movieId
     * @return
     */
    @GetMapping("/check/{movieId}")
    @Operation(summary = "检查用户对电影的关系状态")
    public Result<Integer> checkUserMovieRelation(@PathVariable Long movieId) {
        Long userId = BaseContext.getCurrentId();
        Integer relationType = movieUserService.checkUserMovieRelation(userId, movieId);
        return Result.success(relationType);
    }

    /**
     * 提交/更新评分
     * @param ratingSubmitDTO 评分信息
     * @return 评分结果
     */
    @PostMapping("/rating")
    @Operation(summary = "提交/更新评分")
    public Result<RatingVO> submitRating(@RequestBody @Validated RatingSubmitDTO ratingSubmitDTO) {
        log.info("用户提交评分: {}", ratingSubmitDTO);

        // 从线程局部变量获取用户ID
        Long userId = BaseContext.getCurrentId();

        RatingVO ratingVO = movieUserService.submitRating(userId, ratingSubmitDTO);
        return Result.success(ratingVO);
    }

    /**
     * 获取用户对指定电影的评分
     * @param movieId 电影ID
     * @return 评分信息
     */
    @GetMapping("/rating/{movieId}")
    @Operation(summary = "获取用户对电影的评分")
    public Result<RatingVO> getRating(@PathVariable Long movieId) {
        log.info("获取用户对电影的评分: movieId={}", movieId);
        Long userId = BaseContext.getCurrentId();
        RatingVO ratingVO = movieUserService.getRating(userId, movieId);
        return Result.success(ratingVO);
    }

    /**
     * 获取用户的所有评分列表
     * @return 评分列表
     */
    @GetMapping("/ratings")
    @Operation(summary = "获取用户的所有评分列表")
    public Result<List<RatingVO>> getUserRatings() {
        Long userId = BaseContext.getCurrentId();
        List<RatingVO> ratings = movieUserService.getUserRatings(userId);
        return Result.success(ratings);
    }
}