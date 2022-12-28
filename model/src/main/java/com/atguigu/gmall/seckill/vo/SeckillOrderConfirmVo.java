package com.atguigu.gmall.seckill.vo;

import com.atguigu.gmall.order.entity.OrderDetail;
import com.atguigu.gmall.user.entity.UserAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SeckillOrderConfirmVo {
    private List<OrderDetail> detailArrayList;
    private Integer totalNumber;
    private BigDecimal totalAmount;
    private List<UserAddress> userAddressList;
}
