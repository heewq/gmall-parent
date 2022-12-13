package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.feign.search.SearchFeignClient;
import com.atguigu.gmall.product.entity.*;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.*;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import com.atguigu.gmall.product.vo.SkuSaveVo;
import com.atguigu.gmall.search.Goods;
import com.atguigu.gmall.search.SearchAttr;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vcwfhe
 * @description 针对表【sku_info(库存单元表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo>
        implements SkuInfoService {
    @Autowired
    private SkuImageService skuImageService;
    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private BaseTrademarkService baseTrademarkService;
    @Autowired
    private BaseCategory2Service baseCategory2Service;

    @Override
    public void saveSkuInfo(SkuSaveVo skuSaveVo) {
        // 保存sku_info表
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuSaveVo, skuInfo);
        this.save(skuInfo);

        // 保存sku_image表
        List<SkuImage> skuImageList = skuSaveVo.getSkuImageList()
                .stream()
                .map(item -> {
                    SkuImage skuImage = new SkuImage();
                    BeanUtils.copyProperties(item, skuImage);
                    skuImage.setSkuId(skuInfo.getId());
                    return skuImage;
                }).collect(Collectors.toList());

        skuImageService.saveBatch(skuImageList);

        // 保存平台属性sku_attr_value表
        List<SkuAttrValue> skuAttrValueList = skuSaveVo.getSkuAttrValueList()
                .stream()
                .map(item -> {
                    SkuAttrValue skuAttrValue = new SkuAttrValue();
                    BeanUtils.copyProperties(item, skuAttrValue);
                    skuAttrValue.setSkuId(skuInfo.getId());
                    return skuAttrValue;
                }).collect(Collectors.toList());

        skuAttrValueService.saveBatch(skuAttrValueList);

        // 保存销售属性sku_sale_attr_value表
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaveVo.getSkuSaleAttrValueList()
                .stream()
                .map(item -> {
                    SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
                    skuSaleAttrValue.setSkuId(skuInfo.getId());
                    skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                    skuSaleAttrValue.setSaleAttrValueId(item.getSaleAttrValueId());
                    return skuSaleAttrValue;
                }).collect(Collectors.toList());

        skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);

        // 更新bitmap
        redisTemplate.opsForValue().setBit(RedisConst.SKUID_BITMAP, skuInfo.getId(), true);
    }

    @Autowired
    private SearchFeignClient searchFeignClient;

    @Override
    public void onSale(Long skuId) {
        // 修改数据库中的上架状态
        boolean update = lambdaUpdate()
                .set(SkuInfo::getIsSale, 1)
                .eq(SkuInfo::getId, skuId)
                .update();

        // 保到ES中
        if (update) {
            Goods goods = getGoods(skuId);
            searchFeignClient.onSale(goods);
            // 同步缓存和 bitmap
            redisTemplate.opsForValue().setBit(RedisConst.SKUID_BITMAP, skuId, true);
        }
    }

    @Override
    public void cancelSale(Long skuId) {
        boolean update = lambdaUpdate()
                .set(SkuInfo::getIsSale, 0)
                .eq(SkuInfo::getId, skuId)
                .update();

        if (update) {
            searchFeignClient.cancelSale(skuId);

            // 同步缓存和 bitmap
            redisTemplate.delete(RedisConst.SKU_DETAIL_CACHE + skuId);
            redisTemplate.opsForValue().setBit(RedisConst.SKUID_BITMAP, skuId, false);
        }
    }

    /**
     * 根据skuId查询要上架的商品数据
     *
     * @param skuId
     * @return
     */
    private Goods getGoods(Long skuId) {
        SkuInfo skuInfo = getById(skuId);

        Goods goods = new Goods();
        goods.setId(skuInfo.getId());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setCreateTime(new Date());

        // 查询品牌
        BaseTrademark trademark = baseTrademarkService.getById(skuInfo.getTmId());
        goods.setTmId(skuInfo.getTmId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());

        // 三级分类信息
        CategoryTreeVo categoryTree = baseCategory2Service.getCategoryTreeWithC3Id(skuInfo.getCategory3Id());

        goods.setCategory1Id(categoryTree.getCategoryId());
        goods.setCategory1Name(categoryTree.getCategoryName());

        CategoryTreeVo categoryChild = categoryTree.getCategoryChild().get(0);
        goods.setCategory2Id(categoryChild.getCategoryId());
        goods.setCategory2Name(categoryChild.getCategoryName());

        categoryChild = categoryChild.getCategoryChild().get(0);
        goods.setCategory3Id(categoryChild.getCategoryId());
        goods.setCategory3Name(categoryChild.getCategoryName());

        // 热度分
        goods.setHotScore(0L);

        // 所有平台属性
        List<SearchAttr> attrs = skuAttrValueService.getSkuAttrsAndValue(skuId);
        goods.setAttrs(attrs);
        return goods;
    }
}
