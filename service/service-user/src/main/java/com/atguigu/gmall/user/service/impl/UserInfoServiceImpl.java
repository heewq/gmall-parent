package com.atguigu.gmall.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.user.entity.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserInfoService;
import com.atguigu.gmall.user.vo.LoginSuccessVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author vcwfhe
 * @description 针对表【user_info(用户表)】的数据库操作Service实现
 * @createDate 2022-12-15 18:56:53
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
        implements UserInfoService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public LoginSuccessVo login(UserInfo userInfo) {
        LoginSuccessVo resp = new LoginSuccessVo();

        String loginName = userInfo.getLoginName();
        String passwd = MD5.encrypt(userInfo.getPasswd());

        UserInfo user = lambdaQuery().eq(UserInfo::getLoginName, loginName)
                .eq(UserInfo::getPasswd, passwd).one();
        if (user == null) {
            throw new GmallException(ResultCodeEnum.LOGIN_ERROR);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setNickName(user.getNickName());

        redisTemplate.opsForValue().set(RedisConst.LOGIN_USER + token, JSON.toJSONString(user), 7, TimeUnit.DAYS);

        return resp;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(RedisConst.LOGIN_USER + token);
    }
}
