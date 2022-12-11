package com.atguigu.gmall.starter.cache.annotation;

import com.atguigu.gmall.starter.cache.redisson.RedissonAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(RedissonAutoConfiguration.class)
public @interface EnableRedisson {
}
