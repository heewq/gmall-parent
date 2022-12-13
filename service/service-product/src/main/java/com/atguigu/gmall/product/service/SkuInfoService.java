package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.vo.SkuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author vcwfhe
 * @description 针对表【sku_info(库存单元表)】的数据库操作Service
 * @createDate 2022-11-29 22:22:53
 */
public interface SkuInfoService extends IService<SkuInfo> {

    void saveSkuInfo(SkuSaveVo skuSaveVo);

    void onSale(Long skuId);

    void cancelSale(Long skuId);
}
