package com.fc.controller.admin;

import com.fc.dto.movie.admin.MovieAddDTO;
import com.fc.dto.movie.admin.MovieUpdateDTO;
import com.fc.result.Result;
import com.fc.service.admin.MovieAdminService;
import com.fc.vo.movie.admin.MovieVO;
import com.fc.vo.movie.admin.PosterUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/movie")
@Tag(name = "电影管理接口")
@Slf4j
public class MovieAdminController {

    @Autowired
    private MovieAdminService movieAdminService;

    /**
     * 添加电影信息
     * @param movieAddDTO
     * @return
     */
    @PostMapping("/add")
    @Operation(summary = "添加电影信息")
    public Result<MovieVO> addMovie(@RequestBody @Validated MovieAddDTO movieAddDTO) {
        log.info("添加电影信息:{}", movieAddDTO);
        MovieVO movieVO = movieAdminService.addMovie(movieAddDTO);
        return Result.success(movieVO);
    }

    /**
     * 修改电影信息
     * @param movieUpdateDTO
     * @return
     */
    @PutMapping("/update")
    @Operation(summary = "修改电影信息")
    public Result<MovieVO> updateMovie(@RequestBody @Validated MovieUpdateDTO movieUpdateDTO) {
        log.info("修改电影信息:{}", movieUpdateDTO);
        MovieVO movieVO = movieAdminService.updateMovie(movieUpdateDTO);
        return Result.success(movieVO);
    }

    /**
     * 上传电影海报
     * @param movieId
     * @param posterFile
     * @return
     */
    @PostMapping(value = "/poster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传电影海报")
    public Result<PosterUploadVO> uploadPoster(
            @RequestParam Long movieId,
            @Parameter(description = "海报文件", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("posterFile") MultipartFile posterFile) {

        try {
            log.info("上传电影海报: movieId={}", movieId);
            PosterUploadVO uploadVO = movieAdminService.uploadPoster(movieId, posterFile);
            log.info("电影海报上传成功: movieId={}", movieId);

            return Result.success(uploadVO);
        } catch (Exception e) {
            log.error("电影海报上传失败: movieId={}", movieId, e);
            return Result.error("海报上传失败: " + e.getMessage());
        }
    }

}