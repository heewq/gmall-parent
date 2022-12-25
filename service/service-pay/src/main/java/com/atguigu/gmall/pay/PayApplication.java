package com.atguigu.gmall.pay;

import com.atguigu.gmall.common.config.mq.annotation.EnableMqService;
import com.atguigu.gmall.common.interceptor.annotation.EnableAuthInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableMqService
@EnableAuthInterceptor
@EnableFeignClients(basePackages = {"com.atguigu.gmall.feign.order"})
@SpringCloudApplication
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}
