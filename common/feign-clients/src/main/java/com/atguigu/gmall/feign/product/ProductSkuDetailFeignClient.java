package com.atguigu.gmall.feign.product;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.SkuImage;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("service-product")
@RequestMapping("/api/inner/rpc/product")
public interface ProductSkuDetailFeignClient {
    @GetMapping("category/view/{c3Id}")
    Result<CategoryTreeVo> getCategoryTreeWithC3Id(@PathVariable Long c3Id);

    @GetMapping("/skuInfo/view/{skuId}")
    Result<SkuInfo> getSkuInfo(@PathVariable Long skuId);

    @GetMapping("skuImages/view/{skuId}")
    Result<List<SkuImage>> getImages(@PathVariable Long skuId);

    @GetMapping("/skuPrice/view/{skuId}")
    Result<BigDecimal> getPrice(@PathVariable Long skuId);

    @GetMapping("/spuSaleAttr/view/{spuId}/{skuId}")
    Result<List<SpuSaleAttr>> getSpuSaleAttrs(@PathVariable Long spuId, @PathVariable Long skuId);

    @GetMapping("/valuesSkuJson/{spuId}")
    Result<String> getValuesSkuJson(@PathVariable Long spuId);
}
