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
@Schema(description = "用户注册返回的数据格式")
public class UserRegisterVO {

    private Long userId;
    private String userName;
    private Integer role;
}
