package com.atguigu.gmall.common.config.mq;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.util.MD5;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;

@Service
public class MqService {
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    public MqService(RabbitTemplate rabbitTemplate, StringRedisTemplate redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
        initTemplate();
    }

    public void send(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, JSON.toJSONString(message));
    }

    private void initTemplate() {
        this.rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData,
                                                boolean ack,
                                                @Nullable String cause) -> {
        });

        this.rabbitTemplate.setReturnCallback((Message message,
                                               int replyCode,
                                               String replyText,
                                               String exchange,
                                               String routingKey) -> {
        });

        this.rabbitTemplate.setRetryTemplate(new RetryTemplate());
    }

    /**
     * 有限次数重试
     *
     * @param channel
     * @param deliveryTag
     * @param content
     * @param retryCount
     * @throws IOException
     */
    public void retry(Channel channel, long deliveryTag, String content, Integer retryCount) throws IOException {
        String md5 = MD5.encrypt(content);
        // 同一条消息最多重试 retryCount 次
        Long increment = redisTemplate.opsForValue().increment("msg:count:" + md5);
        Assert.notNull(increment, "");
        if (increment > retryCount) {
            channel.basicAck(deliveryTag, false);
            redisTemplate.delete("msg:count:" + md5);
            // todo 记录到数据库

            return;
        }
        channel.basicNack(deliveryTag, false, true);
    }
}
