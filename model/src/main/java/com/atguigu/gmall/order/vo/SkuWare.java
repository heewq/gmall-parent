package com.atguigu.gmall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuWare {
    // 仓库ID
    private Long wareId;
    private List<Long> skuIds;
}
