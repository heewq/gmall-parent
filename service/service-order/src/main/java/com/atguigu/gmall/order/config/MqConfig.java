package com.atguigu.gmall.order.config;

import com.atguigu.gmall.common.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MqConfig {
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange(MqConst.ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", MqConst.ORDER_EVENT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", MqConst.ORDER_TIMEOUT_RK);
        arguments.put("x-message-ttl", MqConst.ORDER_TTL); // 30 min
        return new Queue(MqConst.ORDER_DELAY_QUEUE,
                true,
                false,
                false,
                arguments);
    }

    @Bean
    public Queue orderDeadQueue() {
        return new Queue(MqConst.ORDER_DEAD_QUEUE,
                true,
                false,
                false);
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(MqConst.ORDER_PAID_QUEUE,
                true,
                false,
                false);
    }

    @Bean
    public Binding delayBinding() {
        return new Binding(MqConst.ORDER_DELAY_QUEUE,
                Binding.DestinationType.QUEUE,
                MqConst.ORDER_EVENT_EXCHANGE,
                MqConst.ORDER_CREAT_RK,
                null);
    }

    @Bean
    public Binding deadBinding() {
        return new Binding(MqConst.ORDER_DEAD_QUEUE,
                Binding.DestinationType.QUEUE,
                MqConst.ORDER_EVENT_EXCHANGE,
                MqConst.ORDER_TIMEOUT_RK,
                null);
    }

    @Bean
    public Binding paidBinding() {
        return new Binding(MqConst.ORDER_PAID_QUEUE,
                Binding.DestinationType.QUEUE,
                MqConst.ORDER_EVENT_EXCHANGE,
                MqConst.ORDER_PAID_RK,
                null);
    }
}
