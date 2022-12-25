package com.atguigu.gmall.user.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.user.entity.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inner/rpc/user")
public class UserRpcController {
    @Autowired
    private UserAddressService userAddressService;

    /**
     * 返回用户所有收获地址列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/addresses/{userId}")
    public Result<List<UserAddress>> getUserAddresses(@PathVariable Long userId) {
        List<UserAddress> addresses = userAddressService.lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .list();
        return Result.ok(addresses);
    }
}
