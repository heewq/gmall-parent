package com.atguigu.gmall.mq.ware;

import lombok.Data;

@Data
public class WareStockResultMsg {
    private Long orderId;
    private String status;
}
