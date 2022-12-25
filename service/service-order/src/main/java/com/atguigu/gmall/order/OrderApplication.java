package com.atguigu.gmall.order;

import com.atguigu.gmall.common.config.mq.annotation.EnableMqService;
import com.atguigu.gmall.common.interceptor.annotation.EnableAuthInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableFeignClients(basePackages = {
        "com.atguigu.gmall.feign.cart",
        "com.atguigu.gmall.feign.product",
        "com.atguigu.gmall.feign.user",
        "com.atguigu.gmall.feign.ware"})
@MapperScan(basePackages = "com.atguigu.gmall.order.mapper")
@EnableAuthInterceptor
@EnableTransactionManagement
@EnableMqService
@SpringCloudApplication
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
