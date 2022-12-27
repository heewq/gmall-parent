package com.atguigu.gmall.mq.ware;

import lombok.Data;

import java.util.List;

@Data
public class WareStockMsg {
    private Long orderId;
    private Long userId;
    private String consignee;
    private String consigneeTel;
    private String orderComment;
    private String orderBody;
    private String deliveryAddress;
    private String paymentWay;
    private List<OrderSku> details;

    @Data
    public static class OrderSku {
        private Long skuId;
        private Integer skuNum;
        private String skuName;
    }
}
