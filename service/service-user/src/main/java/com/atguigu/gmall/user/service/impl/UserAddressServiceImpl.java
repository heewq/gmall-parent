package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.user.entity.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author vcwfhe
 * @description 针对表【user_address(用户地址表)】的数据库操作Service实现
 * @createDate 2022-12-15 18:56:53
 */
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
        implements UserAddressService {

}
