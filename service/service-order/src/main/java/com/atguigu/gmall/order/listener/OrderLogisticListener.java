package com.atguigu.gmall.order.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.config.mq.MqService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.mq.logistic.OrderLogisticMsg;
import com.atguigu.gmall.order.biz.LogisticService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderLogisticListener {
    @Autowired
    private LogisticService logisticService;
    @Autowired
    private MqService mqService;

    @RabbitListener(queues = MqConst.ORDER_LOGISTIC_QUEUE)
    public void listen(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String content = new String(message.getBody());
        try {
            OrderLogisticMsg logisticMsg = JSON.parseObject(content, OrderLogisticMsg.class);

            // 准备电子面单
            JSONObject eOrder = logisticService.generateEOrder(logisticMsg.getOrderId(), logisticMsg.getUserId());

            // 修改订单内容 填写物流号 并标记为已发货
            log.info("电子面单数据: {}", eOrder);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            mqService.retry(channel, deliveryTag, content, 5);
        }
    }
}
