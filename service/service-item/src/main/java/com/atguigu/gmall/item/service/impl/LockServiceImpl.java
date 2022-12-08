package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LockServiceImpl implements LockService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String lock() {
        String uuid = UUID.randomUUID().toString();
        if (Boolean.FALSE.equals(redisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS))) {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return uuid;
    }

    @Override
    public void unLock(String uuid) {
        String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                "    return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end";

        Long lock = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList("lock"), uuid);
    }
}
