package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.user.entity.UserAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmRespVo {
    // 商品列表
    private List<SkuDetail> detailArrayList;
    // 总数量
    private Integer totalNum;
    // 总金额
    private BigDecimal totalAmount;
    // 流水号
    private String tradeNum;
    // 用户收获地址列表
    private List<UserAddress> userAddressList;

    @Data
    public static class SkuDetail {
        private Long skuId;
        private String imgUrl;
        private String skuName;
        private BigDecimal orderPrice;
        private Integer skuNum;
        private String hasStock; // 1: 有货 0: 无货
    }
}
