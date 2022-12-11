package com.atguigu.gmall.starter.cache.aspect.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MallCache {
    /**
     * 指定缓存用的key,支持动态表达式
     *
     * @return
     */
    String cacheKey() default "";

    String bitmapName() default "";

    /**
     * 指定bitmap中需要判定的值,支持动态表达式
     *
     * @return
     */
    String bitmapKey() default "";

    String lockKey() default "";

    long ttl() default 30L;

    TimeUnit unit() default TimeUnit.MINUTES;
}
