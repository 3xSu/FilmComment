package com.fc.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录返回的数据格式")
public class UserLoginVO {

    private Long userId;
    private String username;
    private String token;
    private Integer role;
}
