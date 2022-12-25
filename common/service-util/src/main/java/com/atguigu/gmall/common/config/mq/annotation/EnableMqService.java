package com.atguigu.gmall.common.config.mq.annotation;


import com.atguigu.gmall.common.config.mq.MqService;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MqService.class)
@EnableRabbit
public @interface EnableMqService {
}
