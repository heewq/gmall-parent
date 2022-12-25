package com.atguigu.gmall.common.interceptor.annotation;


import com.atguigu.gmall.common.interceptor.UserAuthFeignInterInterceptor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(UserAuthFeignInterInterceptor.class)
public @interface EnableAuthInterceptor {
}
