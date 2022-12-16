package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.user.entity.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import com.atguigu.gmall.user.vo.LoginSuccessVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class LoginController {
    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("/passport/login")
    public Result login(@RequestBody UserInfo userInfo) {
        LoginSuccessVo loginSuccessVo = userInfoService.login(userInfo);
        return Result.ok(loginSuccessVo);
    }

    @GetMapping("/passport/logout")
    public Result logout(@RequestHeader String token) {
        userInfoService.logout(token);
        return Result.ok();
    }
}
