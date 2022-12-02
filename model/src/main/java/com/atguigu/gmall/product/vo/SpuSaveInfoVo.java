package com.atguigu.gmall.product.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SpuSaveInfoVo {
    private Long id;
    private String spuName;
    private String description;
    private Long category3Id;
    private Long tmId;

    private List<SpuImageDTO> spuImageList;
    private List<SpuSaleAttrDTO> spuSaleAttrList;

    @Data
    @NoArgsConstructor
    public static class SpuImageDTO {
        private String imgName;
        private String imgUrl;
    }

    @Data
    @NoArgsConstructor
    public static class SpuSaleAttrDTO {
        private Long baseSaleAttrId;
        private String saleAttrName;
        private List<SpuSaleAttrValueDTO> spuSaleAttrValueList;

        @Data
        @NoArgsConstructor
        public static class SpuSaleAttrValueDTO {
            private Long baseSaleAttrId;
            private String saleAttrValueName;
        }
    }
}
