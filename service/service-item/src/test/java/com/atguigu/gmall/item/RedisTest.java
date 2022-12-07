package com.atguigu.gmall.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

@SpringBootTest
public class RedisTest {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("A", UUID.randomUUID().toString());
        System.out.println("保存成功");

        String a = redisTemplate.opsForValue().get("A");
        System.out.println("读取到: " + a);
    }
}
