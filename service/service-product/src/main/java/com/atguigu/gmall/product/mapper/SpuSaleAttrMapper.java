package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.vo.ValueSkuJsonVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Mapper
 * @createDate 2022-11-29 22:22:53
 * @Entity com.atguigu.gmall.product.entity.SpuSaleAttr
 */
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    List<SpuSaleAttr> getSpuSaleAttrs(@Param("spuId") Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrWithOrder(@Param("spuId") Long spuId, @Param("skuId") Long skuId);

    List<ValueSkuJsonVo> getValuesSkuJson(@Param("spuId") Long spuId);
}
