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

    /**
     * 查询某个购物车列表
     *
     * @param cartKey
     * @return
     */
    List<CartInfo> getCartInfos(String cartKey);

    /**
     * 修改商品数量
     *
     * @param cartKey
     * @param skuId
     * @param skuNum
     */
    void updateItemNum(String cartKey, Long skuId, Integer skuNum);

    /**
     * 修改选中状态
     *
     * @param cartKey
     * @param skuId
     * @param isChecked
     */
    void check(String cartKey, Long skuId, Integer isChecked);

    /**
     * 删除购物车中某一项
     *
     * @param cartKey
     * @param skuId
     */
    void delete(String cartKey, Long skuId);

    /**
     * 删除选中的商品
     *
     * @param cartKey
     */
    void deleteChecked(String cartKey);

    /**
     * 专供购物车列表使用
     *
     * @return
     */
    List<CartInfo> display();
}
