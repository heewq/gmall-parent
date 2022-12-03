package com.atguigu.gmall.product.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
public class SkuSaveVo {

    private Long id;
    private Long spuId;
    private BigDecimal price;
    private String skuName;
    private BigDecimal weight;
    private String skuDesc;
    private Long category3Id;
    private List<SkuAttrValueListDTO> skuAttrValueList;
    private List<SkuSaleAttrValueListDTO> skuSaleAttrValueList;
    private List<SkuImageListDTO> skuImageList;
    private String skuDefaultImg;
    private Long tmId;

    @NoArgsConstructor
    @Data
    public static class SkuAttrValueListDTO {
        private Long attrId;
        private Long valueId;
    }

    @NoArgsConstructor
    @Data
    public static class SkuSaleAttrValueListDTO {
        private Long saleAttrValueId;
        private String saleAttrValueName;
        private Long baseSaleAttrId;
        private String saleAttrName;
    }

    @NoArgsConstructor
    @Data
    public static class SkuImageListDTO {
        private Long spuImgId;
        private String imgName;
        private String imgUrl;
        private Integer isDefault;
    }
}
