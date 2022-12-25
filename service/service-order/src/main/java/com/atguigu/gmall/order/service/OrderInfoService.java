package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author vcwfhe
 * @description 针对表【order_info(订单表 订单表)】的数据库操作Service
 * @createDate 2022-12-21 23:08:53
 */
public interface OrderInfoService extends IService<OrderInfo> {

    OrderInfo getByOrderIdAndUserId(Long orderId, Long userId);
}
