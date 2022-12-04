package com.atguigu.gmall.product.vo;

import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情数据
 */
@Data
@NoArgsConstructor
public class SkuDetailVo {
    private CategoryViewDTO categoryView;
    private SkuInfo skuInfo;
    private BigDecimal price;
    private List<SpuSaleAttr> spuSaleAttrList;
    private String valuesSkuJson;

    @Data
    @NoArgsConstructor
    public static class CategoryViewDTO {
        private Long category1Id;
        private Long category2Id;
        private Long category3Id;
        private String category1Name;
        private String category2Name;
        private String category3Name;
    }
}
