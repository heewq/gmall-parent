package com.atguigu.gmall.product;


import com.atguigu.gmall.common.config.minio.annotation.EnableMinio;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

//@EnableCircuitBreaker
//@EnableDiscoveryClient
//@SpringBootApplication
@EnableMinio
@MapperScan(basePackages = "com.atguigu.gmall.product.mapper")
@SpringCloudApplication
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
