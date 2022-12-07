package com.atguigu.gmall.item.service;

import com.atguigu.gmall.product.vo.SkuDetailVo;

public interface CacheService {
    SkuDetailVo getFromCache(Long skuId);

    void saveData(Long skuId, SkuDetailVo returnVal);

    Boolean mightContain(Long skuId);
}
