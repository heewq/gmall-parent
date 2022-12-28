package com.atguigu.gmall.mq.seckill;

import lombok.Data;

@Data
public class SeckillOrderMsg {
    private Long userId;
    private String code;
    private Long skuId;
    private String date;
}
