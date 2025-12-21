package com.fc.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户登录时传递的数据模型")
public class UserLoginDTO {

    private String username;
    private String password;
}
