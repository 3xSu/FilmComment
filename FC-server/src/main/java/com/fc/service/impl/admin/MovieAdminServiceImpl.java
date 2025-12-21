package com.fc.service.impl.admin;

import com.fc.constant.MessageConstant;
import com.fc.dto.movie.admin.MovieAddDTO;
import com.fc.dto.movie.admin.MovieUpdateDTO;
import com.fc.entity.Movie;
import com.fc.exception.FileUploadException;
import com.fc.exception.MovieAlreadyExistsException;
import com.fc.exception.MovieNotFoundException;
import com.fc.mapper.admin.MovieAdminMapper;
import com.fc.service.admin.MovieAdminService;
import com.fc.utils.AliOssUtil;
import com.fc.utils.FileSecurityValidator;
import com.fc.vo.movie.admin.MovieVO;
import com.fc.vo.movie.admin.PosterUploadVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MovieAdminServiceImpl implements MovieAdminService {

    @Autowired
    private MovieAdminMapper movieAdminMapper;

    @Autowired
    private AliOssUtil aliOssUtil;


    /**
     * 添加电影信息
     * @param movieAddDTO
     * @return
     */
    @Override
    public MovieVO addMovie(MovieAddDTO movieAddDTO) {
        String title = movieAddDTO.getTitle();

        // 检查电影标题是否已存在
        Movie existingMovie = movieAdminMapper.getByTitle(title);
        if (existingMovie != null) {
            throw new MovieAlreadyExistsException(MessageConstant.MOVIE_ALREADY_EXISTS);
        }

        // 创建新电影
        Movie movie = Movie.builder()
                .title(title)
                .duration(movieAddDTO.getDuration())
                .intro(movieAddDTO.getIntro())
                .posterUrl(movieAddDTO.getPosterUrl())
                .releaseDate(movieAddDTO.getReleaseDate())
                .avgRating(BigDecimal.ZERO) // 初始评分为0
                .ratingCount(0)            // 初始评分人数为0
                .isDeleted(0)              // 未删除
                .build();

        // 插入数据库
        movieAdminMapper.insert(movie);

        // 构建返回结果
        return MovieVO.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .duration(movie.getDuration())
                .intro(movie.getIntro())
                .posterUrl(movie.getPosterUrl())
                .releaseDate(movie.getReleaseDate())
                .avgRating(movie.getAvgRating())
                .ratingCount(movie.getRatingCount())
                .createTime(movie.getCreateTime())
                .updateTime(movie.getUpdateTime())
                .build();
    }

    /**
     * 修改电影信息
     * @param movieUpdateDTO
     * @return
     */
    @Override
    public MovieVO updateMovie(MovieUpdateDTO movieUpdateDTO) {
        Long movieId = movieUpdateDTO.getMovieId();

        // 检查电影是否存在
        Movie existingMovie = movieAdminMapper.getByMovieId(movieId);
        if (existingMovie == null) {
            throw new MovieNotFoundException(MessageConstant.MOVIE_NOT_FOUND);
        }

        // 如果修改了标题，检查新标题是否与其他电影重复
        if (movieUpdateDTO.getTitle() != null &&
                !movieUpdateDTO.getTitle().equals(existingMovie.getTitle())) {
            Movie movieWithSameTitle = movieAdminMapper.getByTitleExcludeId(
                    movieUpdateDTO.getTitle(), movieId);
            if (movieWithSameTitle != null) {
                throw new MovieAlreadyExistsException(MessageConstant.MOVIE_ALREADY_EXISTS);
            }
        }

        // 更新电影信息（只更新非空字段）
        Movie movie = Movie.builder()
                .movieId(movieId)
                .title(movieUpdateDTO.getTitle() != null ? movieUpdateDTO.getTitle() : existingMovie.getTitle())
                .duration(movieUpdateDTO.getDuration() != null ? movieUpdateDTO.getDuration() : existingMovie.getDuration())
                .intro(movieUpdateDTO.getIntro() != null ? movieUpdateDTO.getIntro() : existingMovie.getIntro())
                .posterUrl(movieUpdateDTO.getPosterUrl() != null ? movieUpdateDTO.getPosterUrl() : existingMovie.getPosterUrl())
                .releaseDate(movieUpdateDTO.getReleaseDate() != null ? movieUpdateDTO.getReleaseDate() : existingMovie.getReleaseDate())
                .build();

        // 更新数据库
        movieAdminMapper.update(movie);

        // 获取更新后的电影信息
        Movie updatedMovie = movieAdminMapper.getByMovieId(movieId);

        // 构建返回结果
        return buildMovieVO(updatedMovie);
    }

    /**
     * 构建MovieVO对象
     * @param movie
     * @return
     */
    private MovieVO buildMovieVO(Movie movie) {
        return MovieVO.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .duration(movie.getDuration())
                .intro(movie.getIntro())
                .posterUrl(movie.getPosterUrl())
                .releaseDate(movie.getReleaseDate())
                .avgRating(movie.getAvgRating())
                .ratingCount(movie.getRatingCount())
                .createTime(movie.getCreateTime())
                .updateTime(movie.getUpdateTime())
                .build();
    }

    /**
     * 上传电影海报
     * @param movieId
     * @param posterFile
     * @return
     */
    @Override
    public PosterUploadVO uploadPoster(Long movieId, MultipartFile posterFile) {
        // 检查电影是否存在
        Movie movie = movieAdminMapper.getByMovieId(movieId);
        if (movie == null) {
            throw new MovieNotFoundException(MessageConstant.MOVIE_NOT_FOUND);
        }

        // 检查文件是否为空
        if (posterFile == null || posterFile.isEmpty()) {
            throw new FileUploadException(MessageConstant.FILE_IS_EMPTY);
        }

        // 检查文件类型
        String originalFilename = posterFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileUploadException(MessageConstant.FILE_NAME_INVALID);
        }

        String extension = getSafeExtension(posterFile);
        if (!isImageFile(extension)) {
            throw new FileUploadException(MessageConstant.FILE_TYPE_NOT_SUPPORTED);
        }

        // 检查文件大小（限制为5MB，海报通常比头像大）
        if (posterFile.getSize() > 5 * 1024 * 1024) {
            throw new FileUploadException(MessageConstant.FILE_SIZE_EXCEEDED);
        }

        try {
            // 安全文件验证
            FileSecurityValidator.validateImageFile(posterFile);
            // 生成唯一的文件名
            String fileName = generatePosterFileName(extension);

            // 上传到阿里云OSS
            String posterUrl = aliOssUtil.upload(posterFile.getBytes(), fileName);

            // 更新电影海报信息
            Movie movieToUpdate = Movie.builder()
                    .movieId(movieId)
                    .posterUrl(posterUrl)
                    .build();

            movieAdminMapper.updatePoster(movieToUpdate);

            // 获取更新后的电影信息以获取自动填充的updateTime
            Movie updatedMovie = movieAdminMapper.getByMovieId(movieId);

            // 构建返回结果
            return PosterUploadVO.builder()
                    .movieId(movieId)
                    .posterUrl(posterUrl)
                    .updateTime(updatedMovie.getUpdateTime())
                    .build();

        } catch (IOException e) {
            throw new FileUploadException(MessageConstant.FILE_UPLOAD_FAILED);
        } catch (SecurityException e) {
            log.warn("海报文件安全验证失败: movieId={}, error={}", movieId, e.getMessage());
            throw new FileUploadException("文件安全验证失败: " + e.getMessage());
        }
    }

    /**
     * 安全获取文件扩展名
     * @param file
     * @return
     */
    private String getSafeExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return ".jpg"; // 默认扩展名
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 只允许特定的扩展名
        List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            return ".jpg"; // 强制使用安全扩展名
        }

        return extension;
    }

    /**
     * 检查是否为图片文件
     * @param extension 文件扩展名
     * @return
     */
    private boolean isImageFile(String extension) {
        if (extension == null) return false;

        String lowerExtension = extension.toLowerCase();
        return lowerExtension.equals(".jpg") ||
                lowerExtension.equals(".jpeg") ||
                lowerExtension.equals(".png") ||
                lowerExtension.equals(".webp"); // 支持webp格式
    }

    /**
     * 生成唯一的电影海报文件名
     * @param extension 文件扩展名
     * @return
     */
    private String generatePosterFileName(String extension) {
        return "posters/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }


}
