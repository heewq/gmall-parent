package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.common.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

//@Service
@Slf4j
public class MqListener {

    //@RabbitListener(queues = "haha") // 监听的队列
    public void listen(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String content = new String(message.getBody());
            System.out.println("received message: " + content + "; processing...");


            channel.basicAck(deliveryTag, false);
            System.out.println(deliveryTag + " return ok finish");
        } catch (IOException e) {
            // 出现异常
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
