package com.atguigu.gmall.web;

import com.atguigu.gmall.common.interceptor.annotation.EnableAuthInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringCloudApplication
@EnableAuthInterceptor
@EnableFeignClients(basePackages = {
        "com.atguigu.gmall.feign"
})
public class WebAllApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebAllApplication.class, args);
    }
}
