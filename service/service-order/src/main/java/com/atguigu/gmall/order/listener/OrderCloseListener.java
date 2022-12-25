package com.atguigu.gmall.order.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.config.mq.MqService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class OrderCloseListener {
    @Autowired
    private OrderBizService orderBizService;
    @Autowired
    private MqService mqService;

    /**
     * 监听死信队列中所有待关闭的订单
     *
     * @param message
     * @param channel
     */
    @RabbitListener(queues = MqConst.ORDER_DEAD_QUEUE)
    public void listen(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String content = new String(message.getBody());
        try {
            OrderInfo orderInfo = JSON.parseObject(content, OrderInfo.class);

            // 收到重复消息 保证收消息方业务幂等性
            orderBizService.closeOrder(orderInfo.getId(), orderInfo.getUserId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 防止无限重试
            mqService.retry(channel, deliveryTag, content, 5);
        }
    }
}
