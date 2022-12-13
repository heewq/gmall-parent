package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.entity.SkuAttrValue;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.service.SkuAttrValueService;
import com.atguigu.gmall.search.SearchAttr;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【sku_attr_value(sku平台属性值关联表)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Service
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValue>
        implements SkuAttrValueService {

    @Override
    public List<SearchAttr> getSkuAttrsAndValue(Long skuId) {

        return baseMapper.getSkuAttrsAndValue(skuId);
    }
}
