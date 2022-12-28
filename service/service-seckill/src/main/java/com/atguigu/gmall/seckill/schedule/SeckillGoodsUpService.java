package com.atguigu.gmall.seckill.schedule;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillGoodsUpService {
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 每天晚上2点上架秒杀的商品
    @Scheduled(cron = "0 0 2 * * ?") // 生产环境
//    @Scheduled(cron = "0 * * * * ?")
    public void upGoods() {
        log.info("上架当天参与秒杀的所有商品");
        String date = DateUtil.formatDate(new Date());
        List<SeckillGoods> goods = seckillGoodsService.getSeckillGoodsByDay(date);
        // 缓存到 redis
        goods.forEach(good -> redisTemplate.opsForHash()
                .put(RedisConst.SECKILL_GOODS_CACHE + date, good.getSkuId().toString(), JSON.toJSONString(good)));

        redisTemplate.expire(RedisConst.SECKILL_GOODS_CACHE + date, 2, TimeUnit.DAYS);

        // 把当天参与秒杀的所有商品同步到本地缓存
        seckillGoodsService.saveToLocalCache(goods);
    }
}
