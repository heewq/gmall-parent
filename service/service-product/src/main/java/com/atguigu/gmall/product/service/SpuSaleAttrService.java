package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Service
 * @createDate 2022-11-29 22:22:53
 */
public interface SpuSaleAttrService extends IService<SpuSaleAttr> {

    List<SpuSaleAttr> getSpuSaleAttrs(Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrWithOrder(Long spuId, Long skuId);

    String getValuesSkuJson(Long spuId);
}
