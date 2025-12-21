package com.fc.service.admin;

import com.fc.dto.movie.admin.MovieAddDTO;
import com.fc.dto.movie.admin.MovieUpdateDTO;
import com.fc.vo.movie.admin.MovieVO;
import com.fc.vo.movie.admin.PosterUploadVO;
import org.springframework.web.multipart.MultipartFile;


public interface MovieAdminService {

    /**
     * 添加电影信息
     * @param movieAddDTO
     * @return
     */
    MovieVO addMovie(MovieAddDTO movieAddDTO);

    /**
     * 修改电影信息
     * @param movieUpdateDTO
     * @return
     */
    MovieVO updateMovie(MovieUpdateDTO movieUpdateDTO);

    /**
     * 上传电影海报
     * @param movieId
     * @param posterFile
     * @return
     */
    PosterUploadVO uploadPoster(Long movieId, MultipartFile posterFile);

}
