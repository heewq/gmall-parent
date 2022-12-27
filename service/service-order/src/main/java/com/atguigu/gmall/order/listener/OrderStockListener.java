package com.atguigu.gmall.order.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.config.mq.MqService;
import com.atguigu.gmall.mq.ware.WareStockResultMsg;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 监听库存扣减结果
 */
@Service
@Slf4j
public class OrderStockListener {
    @Autowired
    private MqService mqService;
    @Autowired
    private OrderBizService orderBizService;

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(value = "queue.ware.order", durable = "true", exclusive = "false", autoDelete = "false"),
                    exchange = @Exchange(value = "exchange.direct.ware.order", durable = "true", autoDelete = "false"),
                    key = "ware.order"
            )
    })
    public void listen(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String content = new String(message.getBody());
        try {
            WareStockResultMsg resultMsg = JSON.parseObject(content, WareStockResultMsg.class);
            // 修改订单状态
            orderBizService.updateOrderStatus(resultMsg);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            mqService.retry(channel, deliveryTag, content, 5);
        }
    }
}
