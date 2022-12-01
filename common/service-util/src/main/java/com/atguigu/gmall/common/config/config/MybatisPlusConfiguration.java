package com.atguigu.gmall.common.config.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfiguration {
    @Bean
    MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页拦截器
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor();
        pagination.setOverflow(true); // 自动处理溢出
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
