package com.atguigu.gmall.order.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gmall.common.config.mq.MqService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.mq.ware.WareStockMsg;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.entity.OrderDetail;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.order.entity.PaymentInfo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.service.PaymentInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderPaidListener {
    @Autowired
    private MqService mqService;
    @Autowired
    private OrderBizService orderBizService;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 监听所有的成功支付订单
     *
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = MqConst.ORDER_PAID_QUEUE)
    public void listen(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String content = new String(message.getBody());
        Map<String, String> map = JSON.parseObject(content, new TypeReference<Map<String, String>>() {
        });
        try {

            String outTradeNo = map.get("out_trade_no");
            String[] split = outTradeNo.split("-");
            Long userId = Long.parseLong(split[split.length - 1]);

            // 修改订单为已支付
            orderBizService.paidOrder(outTradeNo, userId);

            // 保存支付回调数据 payment_info
            PaymentInfo paymentInfo = preparePaymentInfo(content, map, outTradeNo, userId);
            paymentInfoService.save(paymentInfo);

            // 支付成功后锁库存 发送消息给库存系统扣减库存
            WareStockMsg wareStockMsg = prepareWareStockMsg(userId, outTradeNo);
            mqService.send(MqConst.WARE_STOCK_EXCHANGE, MqConst.WARE_STOCK_RK, wareStockMsg);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            mqService.retry(channel, deliveryTag, content, 5);
        }
    }

    private WareStockMsg prepareWareStockMsg(Long userId, String outTradeNo) {
        WareStockMsg wareStockMsg = new WareStockMsg();
        OrderInfo orderInfo = orderInfoService.lambdaQuery()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOutTradeNo, outTradeNo)
                .one();
        wareStockMsg.setOrderId(orderInfo.getId());
        wareStockMsg.setUserId(orderInfo.getUserId());
        wareStockMsg.setConsignee(orderInfo.getConsignee());
        wareStockMsg.setConsigneeTel(orderInfo.getConsigneeTel());
        wareStockMsg.setOrderComment(orderInfo.getOrderComment());
        wareStockMsg.setOrderBody(orderInfo.getTradeBody());
        wareStockMsg.setDeliveryAddress(orderInfo.getDeliveryAddress());
        wareStockMsg.setPaymentWay("2");

        List<WareStockMsg.OrderSku> orderSkus = orderDetailService.lambdaQuery()
                .eq(OrderDetail::getOrderId, orderInfo.getId())
                .eq(OrderDetail::getUserId, userId)
                .list()
                .stream()
                .map(orderDetail -> {
                    WareStockMsg.OrderSku orderSku = new WareStockMsg.OrderSku();
                    BeanUtils.copyProperties(orderDetail, orderSku);
                    return orderSku;
                }).collect(Collectors.toList());
        wareStockMsg.setDetails(orderSkus);

        return wareStockMsg;
    }

    private PaymentInfo preparePaymentInfo(String content, Map<String, String> map, String outTradeNo, Long userId) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setUserId(userId);
        OrderInfo orderInfo = orderInfoService.lambdaQuery()
                .eq(OrderInfo::getOutTradeNo, outTradeNo)
                .eq(OrderInfo::getUserId, userId)
                .one();
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(orderInfo.getPaymentWay());
        paymentInfo.setTradeNo(map.get("trade_no"));
        paymentInfo.setTotalAmount(new BigDecimal(map.get("total_amount")));
        paymentInfo.setSubject(map.get("subject"));
        paymentInfo.setPaymentStatus(map.get("trade_status"));
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(content);
        return paymentInfo;
    }
}
