package com.atguigu.gmall.starter.cache;

import com.atguigu.gmall.starter.cache.annotation.EnableRedisson;
import com.atguigu.gmall.starter.cache.aspect.CacheAspect;
import com.atguigu.gmall.starter.cache.redisson.RedissonAutoConfiguration;
import com.atguigu.gmall.starter.cache.service.CacheService;
import com.atguigu.gmall.starter.cache.service.impl.CacheServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CacheAspect.class)
@EnableRedisson
@EnableAspectJAutoProxy
@AutoConfigureAfter({RedisAutoConfiguration.class, RedissonAutoConfiguration.class})
public class CacheAutoConfiguration {
    @Bean
    public CacheService cacheService() {
        return new CacheServiceImpl();
    }
}
