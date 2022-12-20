package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.entity.CartInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    @Autowired
    private CartService cartService;

    @GetMapping("/cartList")
    public Result cartList() {
        List<CartInfo> cartInfos = cartService.display();
        return Result.ok(cartInfos);
    }

    @PostMapping("/addToCart/{skuId}/{skuNum}")
    public Result updateItemNum(@PathVariable Long skuId,
                                @PathVariable Integer skuNum) {
        String cartKey = cartService.determineCartKey();
        cartService.updateItemNum(cartKey, skuId, skuNum);
        return Result.ok();
    }

    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked) {
        String cartKey = cartService.determineCartKey();
        cartService.check(cartKey, skuId, isChecked);
        return Result.ok();
    }

    @DeleteMapping("/deleteCart/{skuId}")
    public Result delete(@PathVariable Long skuId) {
        String cartKey = cartService.determineCartKey();
        cartService.delete(cartKey, skuId);
        return Result.ok();
    }
}
