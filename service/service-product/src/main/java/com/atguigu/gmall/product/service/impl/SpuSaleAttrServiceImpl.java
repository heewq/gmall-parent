package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.vo.ValueSkuJsonVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author vcwfhe
 * @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Service实现
 * @createDate 2022-11-29 22:22:53
 */
@Slf4j
@Service
public class SpuSaleAttrServiceImpl extends ServiceImpl<SpuSaleAttrMapper, SpuSaleAttr>
        implements SpuSaleAttrService {
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrs(Long spuId) {
        return baseMapper.getSpuSaleAttrs(spuId);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrWithOrder(Long spuId, Long skuId) {
        return baseMapper.getSpuSaleAttrWithOrder(spuId, skuId);
    }

    @Override
    public String getValuesSkuJson(Long spuId) {
        List<ValueSkuJsonVo> valueSkuJsonVos = baseMapper.getValuesSkuJson(spuId);
        Map<String, Long> valueJsonMap = valueSkuJsonVos
                .stream()
                .collect(Collectors.toMap(ValueSkuJsonVo::getValueJson, ValueSkuJsonVo::getSkuId));
        String valueJson = JSON.toJSONString(valueJsonMap);
        log.info(valueJson);
        return valueJson;
    }
}
