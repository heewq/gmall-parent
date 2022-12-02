package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.entity.SpuInfo;
import com.atguigu.gmall.product.vo.SpuSaveInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author vcwfhe
 * @description 针对表【spu_info(商品表)】的数据库操作Service
 * @createDate 2022-11-29 22:22:53
 */
public interface SpuInfoService extends IService<SpuInfo> {
    void saveSpuInfo(SpuSaveInfoVo spuSaveInfoVo);
}
