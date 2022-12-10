package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.item.service.CacheService;
import com.atguigu.gmall.product.vo.SkuDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public SkuDetailVo getFromCache(Long skuId) {
        // "sku:info:"
//        log.info("正在查询缓存...");
        String json = redisTemplate.opsForValue().get(RedisConst.SKU_DETAIL_CACHE + skuId);

        if (StringUtils.isEmpty(json)) {
            return null;
        } else if ("x".equals(json)) {
            return new SkuDetailVo();
        } else {
//            log.info("缓存命中");
            return JSON.parseObject(json, SkuDetailVo.class);
        }
    }

    @Override
    public void saveData(Long skuId, Object returnVal) {
        String jsonString = "x";
        if (returnVal != null) {
            jsonString = JSON.toJSONString(returnVal);
        }
        redisTemplate.opsForValue()
                .set(RedisConst.SKU_DETAIL_CACHE + skuId, jsonString, 7, TimeUnit.DAYS);
    }

    @Override
    public Boolean mightContain(Long skuId) {
        return redisTemplate.opsForValue().getBit(RedisConst.SKUID_BITMAP, skuId);
    }

    @Override
    public Object getCacheDate(String key, Type returnType) {
        String json = redisTemplate.opsForValue().get(key);

        if (StringUtils.isEmpty(json)) {
            return null;
        } else if ("x".equals(json)) {
            return new Object();
        } else {
//            log.info("缓存命中");
            return JSON.parseObject(json, returnType);
        }
    }

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
}
