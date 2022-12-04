package com.atguigu.gmall.item.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.product.vo.SkuDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inner/rpc/item")
public class SkuDetailRpcController {
    @Autowired
    private SkuDetailService skuDetailService;

    /**
     * 获取商品详情数据
     *
     * @param skuId
     * @return
     */
    @GetMapping("/sku/detail/{skuId}")
    public Result<SkuDetailVo> getSkuDetails(@PathVariable Long skuId) {
        // 获取商品详情数据
        SkuDetailVo skuDetailVo = skuDetailService.getSkuDetailData(skuId);
        return Result.ok(skuDetailVo);
    }
}
