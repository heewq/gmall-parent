package com.atguigu.gmall.product.rpc;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.SkuImage;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inner/rpc/product")
public class SkuRpcController {
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImageService skuImageService;
    @Autowired
    private SpuSaleAttrService spuSaleAttrService;

    @GetMapping("/skuInfo/view/{skuId}")
    public Result<SkuInfo> getSkuInfo(@PathVariable Long skuId) {
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        return Result.ok(skuInfo);
    }

    @GetMapping("skuImages/view/{skuId}")
    public Result<List<SkuImage>> getImages(@PathVariable Long skuId) {
        List<SkuImage> skuImages
                = skuImageService.lambdaQuery().eq(SkuImage::getSkuId, skuId).list();
        return Result.ok(skuImages);
    }

    @GetMapping("/skuPrice/view/{skuId}")
    public Result<BigDecimal> getPrice(@PathVariable Long skuId) {
        SkuInfo skuInfoPrice = skuInfoService.lambdaQuery()
                // select price from sku_info where id = ?
                .select(SkuInfo::getPrice)
                .eq(SkuInfo::getId, skuId)
                .one();
        return Result.ok(skuInfoPrice.getPrice());
    }

    @GetMapping("/spuSaleAttr/view/{spuId}/{skuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrs(@PathVariable Long spuId, @PathVariable Long skuId) {
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrService.getSpuSaleAttrWithOrder(spuId, skuId);
        return Result.ok(spuSaleAttrs);
    }

    @GetMapping("/valuesSkuJson/{spuId}")
    public Result<String> getValuesSkuJson(@PathVariable Long spuId) {
        String json = spuSaleAttrService.getValuesSkuJson(spuId);
        return Result.ok(json);
    }
}
