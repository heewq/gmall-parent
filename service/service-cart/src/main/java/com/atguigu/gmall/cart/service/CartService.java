package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.CartInfo;
import com.atguigu.gmall.product.entity.SkuInfo;

import java.util.List;

public interface CartService {
    /**
     * 指定的购物车
     *
     * @return
     */
    String determineCartKey();

    SkuInfo addToCart(Long skuId, Integer skuNum, String cartKey);

    /**
     * 从购物车中获取一个商品
     *
     * @param cartKey
     * @param skuId
     * @return
     */
    CartInfo get(String cartKey, Long skuId);

    /**
     * 保存一项到购物车
     *
     * @param cartKey
     * @param cartInfo
     */
    void save(String cartKey, CartInfo cartInfo);

    List<CartInfo> getList(String cartKey);
}
