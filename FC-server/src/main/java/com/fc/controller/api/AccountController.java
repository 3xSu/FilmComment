package com.fc.controller.api;

import com.fc.constant.JwtClaimsConstant;
import com.fc.context.BaseContext;
import com.fc.dto.user.UserLoginDTO;
import com.fc.dto.user.UserRegisterDTO;
import com.fc.entity.User;
import com.fc.properties.JwtProperties;
import com.fc.result.Result;
import com.fc.service.api.AccountService;
import com.fc.utils.JwtUtil;
import com.fc.vo.user.AvatarUploadVO;
import com.fc.vo.user.UserLoginVO;
import com.fc.vo.user.UserRegisterVO;
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

import java.util.HashMap;
import java.util.Map;

/**
 *用户管理
 */
@RestController
@RequestMapping("/api")
@Tag(name = "账号公共接口")
@Slf4j
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录:{}", userLoginDTO);

        User user = accountService.login(userLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
        String token = JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 用户注册
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserRegisterVO> register(@RequestBody @Validated UserRegisterDTO userRegisterDTO) {
        log.info("用户注册:{}", userRegisterDTO);
        UserRegisterVO userRegisterVO = accountService.register(userRegisterDTO);
        return Result.success(userRegisterVO);
    }

    /**
     * 用户头像上传
     * @param
     * @param avatarFile
     * @return
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "用户头像上传")
    public Result<AvatarUploadVO> uploadAvatar(
            @Parameter(description = "头像文件", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("avatarFile") MultipartFile avatarFile) {

        try {
            // 直接从线程局部变量获取用户ID
            Long userId = BaseContext.getCurrentId();

            AvatarUploadVO uploadVO = accountService.uploadAvatar(userId, avatarFile);
            log.info("用户头像上传成功: userId={}", userId);

            return Result.success(uploadVO);
        } catch (Exception e) {
            log.error("用户头像上传失败", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}
