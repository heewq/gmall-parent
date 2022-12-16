package com.atguigu.gmall.user.vo;

import lombok.Data;

@Data
public class LoginSuccessVo {
    private String token;
    private Long userId;
    private String nickName;
}
