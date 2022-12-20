package com.atguigu.gmall.cart.rpc;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inner/rpc/cart")
public class CartRpcController {
    @Autowired
    private CartService cartService;

    /**
     * 把商品添加到购物车
     *
     * @param skuId
     * @param skuNum
     * @return
     */
    @GetMapping("/add/{skuId}/{skuNum}")
    public Result<SkuInfo> addCart(@PathVariable Long skuId,
                                   @PathVariable Integer skuNum) {
        // 用哪个购物车
        String cartKey = cartService.determineCartKey();
        // 把指定商品添加到指定购物车
        SkuInfo skuInfo = cartService.addToCart(skuId, skuNum, cartKey);
        return Result.ok(skuInfo);
    }
}
