package com.atguigu.gmall.web.feign;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.vo.SkuDetailVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("service-item")
@RequestMapping("/api/inner/rpc/item")
public interface SkuDetailFeignClient {
    @GetMapping("/sku/detail/{skuId}")
    Result<SkuDetailVo> getSkuDetails(@PathVariable Long skuId);
}
