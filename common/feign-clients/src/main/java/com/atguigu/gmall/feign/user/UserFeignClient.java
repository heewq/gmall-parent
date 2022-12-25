package com.atguigu.gmall.feign.user;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.user.entity.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("service-user")
@RequestMapping("/api/inner/rpc/user")
public interface UserFeignClient {
    /**
     * 返回用户所有收获地址列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/addresses/{userId}")
    Result<List<UserAddress>> getUserAddresses(@PathVariable Long userId);
}
