package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author vcwfhe
 * @description 针对表【sku_info(库存单元表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo>
        implements SkuInfoService {

}
