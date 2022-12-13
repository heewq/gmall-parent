package com.atguigu.gmall.search.vo;

import com.atguigu.gmall.search.Goods;
import lombok.Data;

import java.util.List;

/**
 * 检索完成响应结果
 */
@Data
public class SearchRespVo {
    private SearchParamVo searchParam;
    private String trademarkParam;
    private List<Props> propsParamList;
    private List<Trademark> trademarkList;
    private List<Attrs> attrsList;
    private String urlParam;
    private OrderMap orderMap;
    private List<Goods> goodsList;
    private Integer pageNo;
    private Long totalPages;

    @Data
    public static class Props {
        private Long attrId;
        private String attrValue;
        private String attrName;
    }

    @Data
    public static class Trademark {
        private Long tmId;
        private String tmName;
        private String tmLogoUrl;
    }

    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private List<String> attrValueList;
    }

    @Data
    public static class OrderMap {
        private String type;
        private String sort;
    }
}
