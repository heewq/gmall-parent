package com.atguigu.gmall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderSplitResp {
    private Long orderId;
    private Long userId;
    private String consignee;
    private String consigneeTel;
    private String orderComment;
    private String orderBody;
    private String deliveryAddress;
    private String paymentWay;
    private List<OrderSku> details;
    private Long wareId;

    @Data
    public static class OrderSku {
        private Long skuId;
        private Integer skuNum;
        private String skuName;
    }
}
