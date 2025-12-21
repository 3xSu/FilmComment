package com.fc.service.api;

import com.fc.dto.user.UserLoginDTO;
import com.fc.dto.user.UserRegisterDTO;
import com.fc.entity.User;
import com.fc.vo.user.AvatarUploadVO;
import com.fc.vo.user.UserRegisterVO;
import org.springframework.web.multipart.MultipartFile;

public interface AccountService {

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);

    /**
     * 用户注册
     * @param userRegisterDTO
     * @return
     */
    UserRegisterVO register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户上传头像
     * @param userId
     * @param avatarFile
     * @return
     */
    AvatarUploadVO uploadAvatar(Long userId, MultipartFile avatarFile);
}
