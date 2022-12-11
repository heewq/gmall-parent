package com.atguigu.gmall.starter.cache.annotation;

import com.atguigu.gmall.starter.cache.CacheAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(CacheAutoConfiguration.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableAppCache {
}
