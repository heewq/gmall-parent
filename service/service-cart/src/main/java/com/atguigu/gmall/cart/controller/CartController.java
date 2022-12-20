package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.entity.CartInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/cartList")
    public Result cartList() {
        String cartKey = cartService.determineCartKey();
        List<CartInfo> cartInfos = cartService.getList(cartKey);
        return Result.ok(cartInfos);
    }
}
