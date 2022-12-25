package com.atguigu.gmall.order.biz;

import com.atguigu.gmall.order.vo.OrderConfirmRespVo;
import com.atguigu.gmall.order.vo.OrderSubmitVo;

public interface OrderBizService {
    /**
     * 获取订单确认数据
     *
     * @return
     */
    OrderConfirmRespVo getConfirmData();

    /**
     * 下单
     *
     * @param tradeNo
     * @param submitVo
     * @return
     */
    Long submitOrder(String tradeNo, OrderSubmitVo submitVo);

    /**
     * 关闭订单
     *
     * @param orderId
     * @param userId
     */
    void closeOrder(Long orderId, Long userId);
}
