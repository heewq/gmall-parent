package com.atguigu.gmall.mq.logistic;

import lombok.Data;

@Data
public class OrderLogisticMsg {
    private Long orderId;
    private Long userId;
}
