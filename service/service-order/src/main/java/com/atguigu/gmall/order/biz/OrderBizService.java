package com.atguigu.gmall.order.biz;

import com.atguigu.gmall.mq.ware.WareStockResultMsg;
import com.atguigu.gmall.order.vo.OrderConfirmRespVo;
import com.atguigu.gmall.order.vo.OrderSplitResp;
import com.atguigu.gmall.order.vo.OrderSubmitVo;

import java.util.List;

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

    /**
     * 修改订单为已支付
     *
     * @param outTradeNo
     * @param userId
     */
    void paidOrder(String outTradeNo, Long userId);

    void updateOrderStatus(WareStockResultMsg resultMsg);

    List<OrderSplitResp> orderSplit(Long orderId, String json);
}
