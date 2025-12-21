package com.fc.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户上传头像返回的数据格式")
public class AvatarUploadVO {

    private Long userId;
    private String avatarUrl;
    private LocalDateTime updateTime;
}
