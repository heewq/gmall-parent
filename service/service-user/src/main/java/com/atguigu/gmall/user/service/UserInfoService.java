package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.entity.UserInfo;
import com.atguigu.gmall.user.vo.LoginSuccessVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author vcwfhe
 * @description 针对表【user_info(用户表)】的数据库操作Service
 * @createDate 2022-12-15 18:56:53
 */
public interface UserInfoService extends IService<UserInfo> {

    LoginSuccessVo login(UserInfo userInfo);

    void logout(String token);
}
