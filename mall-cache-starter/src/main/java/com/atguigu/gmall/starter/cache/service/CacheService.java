package com.atguigu.gmall.starter.cache.service;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

public interface CacheService {
    /**
     * 从缓存中获取指定类型的数据
     *
     * @param key
     * @param returnType
     * @return
     */
    Object getCacheDate(String key, Type returnType);

    /**
     * 判断指定bitmap中是否有指定数据
     *
     * @param bitmap
     * @param bitmapKey
     * @return
     */
    Boolean mightContain(String bitmap, Long bitmapKey);

    void saveCache(String cacheKey, Object returnVal, long ttl, TimeUnit unit);

    /**
     * 延迟双删
     *
     * @param cacheKey
     */
    void delayDoubleDel(String cacheKey);
}
