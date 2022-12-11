package com.atguigu.gmall.starter.cache.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.starter.cache.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 延迟双删
    ScheduledExecutorService pool = Executors.newScheduledThreadPool(16);

    @Override
    public Boolean mightContain(String bitmap, Long bitmapKey) {
        return redisTemplate.opsForValue().getBit(bitmap, bitmapKey);
    }

    @Override
    public void saveCache(String cacheKey, Object returnVal, long ttl, TimeUnit unit) {
        String jsonString = "x";
        if (returnVal != null) {
            jsonString = JSON.toJSONString(returnVal);
        }
        redisTemplate.opsForValue()
                .set(cacheKey, jsonString, ttl, unit);
    }

    @Override
    public Object getCacheDate(String key, Type returnType) {
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else if ("x".equals(json)) {
            return new Object();
        } else {
            //log.info("缓存命中");
            return JSON.parseObject(json, returnType);
        }
    }

    @Override
    public void delayDoubleDel(String cacheKey) {
        redisTemplate.delete(cacheKey);
        pool.schedule(() -> redisTemplate.delete(cacheKey), 10, TimeUnit.SECONDS);
    }
}
