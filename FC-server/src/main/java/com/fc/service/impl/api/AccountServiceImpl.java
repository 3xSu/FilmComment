package com.fc.service.impl.api;

import com.fc.constant.MessageConstant;
import com.fc.dto.user.UserLoginDTO;
import com.fc.dto.user.UserRegisterDTO;
import com.fc.entity.User;
import com.fc.exception.AccountAlreadyExistsException;
import com.fc.exception.AccountNotFoundException;
import com.fc.exception.FileUploadException;
import com.fc.exception.PasswordErrorException;
import com.fc.mapper.api.AccountMapper;
import com.fc.service.api.AccountService;
import com.fc.utils.AliOssUtil;
import com.fc.vo.user.AvatarUploadVO;
import com.fc.vo.user.UserRegisterVO;
import com.fc.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Value("${fc.default.avatar-url}")
    private String defaultAvatarUrl;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    public User login(UserLoginDTO userLoginDTO){
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        User user = accountMapper.getByUsername(username);

        if (user == null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        return user;
    }


    public UserRegisterVO register(UserRegisterDTO userRegisterDTO){
        String username = userRegisterDTO.getUsername();
        String password = userRegisterDTO.getPassword();

        // 检查用户名是否已存在
        User existingUser = accountMapper.getByUsername(username);
        if (existingUser != null) {
            throw new AccountAlreadyExistsException(MessageConstant.USERNAME_ALREADY_EXISTS);
        }

        // 密码加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        // 创建新用户
        User user = User.builder()
                .username(username)
                .password(encryptedPassword)
                .avatarUrl(defaultAvatarUrl)
                .role(1) // 默认角色，可以根据需要调整
                .build();

        // 插入数据库
        accountMapper.insert(user);

        // 构建返回结果
        return UserRegisterVO.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .role(user.getRole())
                .build();
    }

    /**
     * 用户上传头像
     * @param userId
     * @param avatarFile
     * @return
     */
    public AvatarUploadVO uploadAvatar(Long userId, MultipartFile avatarFile){
        // 检查用户是否存在
        User user = accountMapper.getByUserId(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 检查文件是否为空
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new FileUploadException(MessageConstant.FILE_IS_EMPTY);
        }

        // 检查文件类型
        String originalFilename = avatarFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileUploadException(MessageConstant.FILE_NAME_INVALID);
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!isImageFile(extension)) {
            throw new FileUploadException(MessageConstant.FILE_TYPE_NOT_SUPPORTED);
        }

        // 检查文件大小（限制为2MB）
        if (avatarFile.getSize() > 2 * 1024 * 1024) {
            throw new FileUploadException(MessageConstant.FILE_SIZE_EXCEEDED);
        }

        try {
            // 生成唯一的文件名
            String fileName = generateFileName(extension);

            // 上传到阿里云OSS
            String avatarUrl = aliOssUtil.upload(avatarFile.getBytes(), fileName);

            user.setAvatarUrl(avatarUrl);

            // 更新用户头像信息（AutoFill会自动填充updateTime）
            accountMapper.updateAvatar(user);

            // 获取更新后的用户信息以获取自动填充的updateTime
            User updatedUser = accountMapper.getByUserId(userId);

            // 构建返回结果
            return AvatarUploadVO.builder()
                    .userId(userId)
                    .avatarUrl(avatarUrl)
                    .updateTime(updatedUser.getUpdateTime()) // 使用自动填充的时间
                    .build();

        } catch (IOException e) {
            throw new FileUploadException(MessageConstant.FILE_UPLOAD_FAILED);
        }
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
                lowerExtension.equals(".png");
    }

    /**
     * 生成唯一的文件名
     * @param extension 文件扩展名
     * @return
     */
    private String generateFileName(String extension) {
        return "avatars/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 获取用户基本信息
     * @param userId 用户ID
     * @return 用户基本信息
     */
    @Override
    public UserVO getUserInfo(Long userId) {
        log.info("获取用户基本信息: userId={}", userId);

        // 查询用户信息
        User user = accountMapper.getByUserId(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 构建返回结果
        return UserVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
