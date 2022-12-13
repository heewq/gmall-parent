package com.atguigu.gmall.product;


import com.atguigu.gmall.common.config.minio.annotation.EnableMinio;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@EnableCircuitBreaker
//@EnableDiscoveryClient
//@SpringBootApplication
@EnableMinio
@MapperScan(basePackages = "com.atguigu.gmall.product.mapper")
@EnableFeignClients(basePackages = "com.atguigu.gmall.feign.search")
@SpringCloudApplication
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
