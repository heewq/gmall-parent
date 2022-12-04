package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.feign.SkuDetailFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.product.entity.SkuImage;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import com.atguigu.gmall.product.vo.SkuDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.atguigu.gmall.product.vo.SkuDetailVo.CategoryViewDTO;

@Service
public class SkuDetailServiceImpl implements SkuDetailService {
    @Autowired
    private SkuDetailFeignClient skuDetailFeignClient;

    @Override
    public SkuDetailVo getSkuDetailData(Long skuId) {
        SkuDetailVo data = new SkuDetailVo();

        // 商品详情
        SkuInfo skuInfo = skuDetailFeignClient.getSkuInfo(skuId).getData();
        List<SkuImage> images = skuDetailFeignClient.getImages(skuId).getData();
        skuInfo.setSkuImageList(images);
        data.setSkuInfo(skuInfo);

        // 当前商品精确的完整分类信息
        CategoryTreeVo categoryTree
                = skuDetailFeignClient.getCategoryTreeWithC3Id(skuInfo.getCategory3Id()).getData();
        CategoryViewDTO categoryViewDTO = new CategoryViewDTO();
        categoryViewDTO.setCategory1Id(categoryTree.getCategoryId());
        categoryViewDTO.setCategory1Name(categoryTree.getCategoryName());
        categoryViewDTO.setCategory2Id(categoryTree.getCategoryChild().get(0).getCategoryId());
        categoryViewDTO.setCategory2Name(categoryTree.getCategoryChild().get(0).getCategoryName());
        categoryViewDTO.setCategory3Id(categoryTree.getCategoryChild().get(0).getCategoryChild().get(0).getCategoryId());
        categoryViewDTO.setCategory3Name(categoryTree.getCategoryChild().get(0).getCategoryChild().get(0).getCategoryName());
        data.setCategoryView(categoryViewDTO);

        // 实时价格
        BigDecimal price = skuDetailFeignClient.getPrice(skuId).getData();
        data.setPrice(price);

        // 销售属性列表
        List<SpuSaleAttr> spuSaleAttrs = skuDetailFeignClient.getSpuSaleAttrs(skuInfo.getSpuId(), skuId).getData();
        data.setSpuSaleAttrList(spuSaleAttrs);

        // 同spu下的所有sku(所有销售属性组合)
        String valuesSkuJson = skuDetailFeignClient.getValuesSkuJson(skuInfo.getSpuId()).getData();
        data.setValuesSkuJson(valuesSkuJson);

        return data;
    }
}
