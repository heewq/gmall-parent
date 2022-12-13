package com.atguigu.gmall.item.service;

import com.atguigu.gmall.product.vo.SkuDetailVo;

public interface SkuDetailService {
    SkuDetailVo getSkuDetailData(Long skuId);

    /**
     * 增加商品热度分 hotScore
     *
     * @param skuId
     */
    void incrHotScore(Long skuId);
}
