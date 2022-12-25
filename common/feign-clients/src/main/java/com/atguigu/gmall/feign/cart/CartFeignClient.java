package com.atguigu.gmall.feign.cart;

import com.atguigu.gmall.cart.entity.CartInfo;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.SkuInfo;
import feign.Request;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("service-cart")
@RequestMapping("/api/inner/rpc/cart")
public interface CartFeignClient {
    /**
     * 把商品添加到购物车
     *
     * @param skuId
     * @param skuNum
     * @return
     */
    @GetMapping("/add/{skuId}/{skuNum}")
    Result<SkuInfo> addCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            Request.Options options
    );

    /**
     * 删除选中的
     *
     * @return
     */
    @DeleteMapping("/deleteChecked")
    Result deleteChecked();

    /**
     * 获取购物车选中的所有商品
     *
     * @return
     */
    @GetMapping("/checked")
    Result<List<CartInfo>> getChecked();
}
