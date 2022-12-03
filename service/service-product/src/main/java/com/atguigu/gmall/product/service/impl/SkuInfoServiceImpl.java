package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.SkuAttrValue;
import com.atguigu.gmall.product.entity.SkuImage;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.entity.SkuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.SkuAttrValueService;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.atguigu.gmall.product.vo.SkuSaveVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    }
}
