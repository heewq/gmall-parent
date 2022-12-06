package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.feign.SkuDetailFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import com.atguigu.gmall.product.vo.SkuDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.atguigu.gmall.product.vo.SkuDetailVo.CategoryViewDTO;

@Service
public class SkuDetailServiceImpl implements SkuDetailService {
    @Autowired
    private SkuDetailFeignClient skuDetailFeignClient;
    @Autowired
    private ThreadPoolExecutor executor;

    private static CategoryViewDTO getCategoryViewDTO(CategoryTreeVo categoryTree) {
        CategoryViewDTO categoryViewDTO = new CategoryViewDTO();
        categoryViewDTO.setCategory1Id(categoryTree.getCategoryId());
        categoryViewDTO.setCategory1Name(categoryTree.getCategoryName());
        categoryViewDTO.setCategory2Id(categoryTree.getCategoryChild().get(0).getCategoryId());
        categoryViewDTO.setCategory2Name(categoryTree.getCategoryChild().get(0).getCategoryName());
        categoryViewDTO.setCategory3Id(categoryTree.getCategoryChild().get(0).getCategoryChild().get(0).getCategoryId());
        categoryViewDTO.setCategory3Name(categoryTree.getCategoryChild().get(0).getCategoryChild().get(0).getCategoryName());
        return categoryViewDTO;
    }

    @Override
    public SkuDetailVo getSkuDetailData(Long skuId) {
        SkuDetailVo data = new SkuDetailVo();

        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture
                .supplyAsync(() -> skuDetailFeignClient.getImages(skuId).getData(), executor)
                .thenApplyAsync(images -> {
                    // 商品详情
                    SkuInfo skuInfo = skuDetailFeignClient.getSkuInfo(skuId).getData();
                    skuInfo.setSkuImageList(images);
                    data.setSkuInfo(skuInfo);
                    return skuInfo;
                }, executor);

        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture
                .thenAcceptAsync(res -> {
                    CategoryTreeVo categoryTree = skuDetailFeignClient.getCategoryTreeWithC3Id(res.getCategory3Id()).getData();
                    CategoryViewDTO categoryViewDTO = getCategoryViewDTO(categoryTree);
                    data.setCategoryView(categoryViewDTO);
                }, executor);

        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal price = skuDetailFeignClient.getPrice(skuId).getData();
            data.setPrice(price);
        }, executor);

        CompletableFuture<Void> saleAttrCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(res -> {
            List<SpuSaleAttr> spuSaleAttrs = skuDetailFeignClient.getSpuSaleAttrs(res.getSpuId(), skuId).getData();
            data.setSpuSaleAttrList(spuSaleAttrs);
        }, executor);

        CompletableFuture<Void> valueSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(res -> {
            String valuesSkuJson = skuDetailFeignClient.getValuesSkuJson(res.getSpuId()).getData();
            data.setValuesSkuJson(valuesSkuJson);
        }, executor);

        CompletableFuture.allOf(
                        categoryViewCompletableFuture,
                        priceCompletableFuture,
                        saleAttrCompletableFuture,
                        valueSkuJsonCompletableFuture)
                .join();

        return data;
    }
}
