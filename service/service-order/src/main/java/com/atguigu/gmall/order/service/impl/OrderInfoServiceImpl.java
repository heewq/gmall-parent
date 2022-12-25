package com.atguigu.gmall.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import org.springframework.stereotype.Service;

/**
 * @author vcwfhe
 * @description 针对表【order_info(订单表 订单表)】的数据库操作Service实现
 * @createDate 2022-12-21 23:08:53
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

    @Override
    public OrderInfo getByOrderIdAndUserId(Long orderId, Long userId) {
        return lambdaQuery()
                .eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getUserId, userId)
                .one();
    }
}
