package com.atguigu.gmall.product.init;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InitRunner implements CommandLineRunner {
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void initBitMap() {
        log.info("正在初始化 skuid - bitmap ...");
        List<SkuInfo> skuInfoIds = skuInfoService
                .lambdaQuery()
                .select(SkuInfo::getId, SkuInfo::getIsSale)
                .list();

        skuInfoIds
                .stream()
                .parallel()
                .forEach(skuInfo -> {
                    if (skuInfo.getIsSale() == 1) {
                        redisTemplate.opsForValue().setBit(RedisConst.SKUID_BITMAP, skuInfo.getId(), true);
                    }
                });

        log.info("初始 skuid - bitmap 化完成");
    }

    @Override
    public void run(String... args) throws Exception {
        initBitMap();
    }
}
