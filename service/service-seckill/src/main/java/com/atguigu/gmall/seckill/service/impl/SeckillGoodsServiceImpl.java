package com.atguigu.gmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.PaymentWay;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.mq.seckill.SeckillOrderMsg;
import com.atguigu.gmall.order.entity.OrderDetail;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.atguigu.gmall.seckill.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author vcwfhe
 * @description 针对表【seckill_goods】的数据库操作Service实现
 * @createDate 2022-12-27 20:16:44
 */
@Service
@Slf4j
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods>
        implements SeckillGoodsService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 引入本地缓存
    Map<Long, SeckillGoods> localCache = new ConcurrentHashMap<>();

    @Override
    public List<SeckillGoods> getSeckillGoodsByDay(String date) {
        return baseMapper.getSeckillGoodsByDay(date);
    }

    @Override
    public List<SeckillGoods> getSeckillGoodsByDayFromCache(String date) {
        // 先查询本地缓存
        List<SeckillGoods> cache = localCache.values().stream()
                .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                .collect(Collectors.toList());
        if (cache.size() == 0) {
            log.info("本地缓存未命中 正在查询 redis");
            cache = redisTemplate.opsForHash().values(RedisConst.SECKILL_GOODS_CACHE + date).stream()
                    .map(good -> JSON.parseObject(good.toString(), SeckillGoods.class))
                    .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                    .collect(Collectors.toList());
            // 保存到本地缓存
            saveToLocalCache(cache);
        }

        return cache;
    }

    @Override
    public void saveToLocalCache(List<SeckillGoods> goods) {
        goods.forEach(good -> localCache.put(good.getSkuId(), good));
    }

    @Override
    public SeckillGoods getDetail(Long skuId) {
        SeckillGoods seckillGoods = localCache.get(skuId);
        if (seckillGoods == null) {
            String date = DateUtil.formatDate(new Date());
            // 同步 redis 和本地缓存
            getSeckillGoodsByDayFromCache(date);
        }
        return localCache.get(skuId);
    }

    @Override
    public void deduceStock(Long id) {
        baseMapper.updateStock(id);
    }

    @Override
    public void saveSeckillOrder(SeckillOrderMsg seckillOrderMsg) {
        Long skuId = seckillOrderMsg.getSkuId();
        SeckillGoods detail = getDetail(skuId);
        // 准备订单数据
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTotalAmount(detail.getCostPrice());
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        orderInfo.setUserId(seckillOrderMsg.getUserId());
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());
        orderInfo.setOutTradeNo("ATGUIGU-" + System.currentTimeMillis() + "-" + seckillOrderMsg.getUserId());
        orderInfo.setTradeBody(detail.getSkuName());
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 30 * 60 * 1000L));
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfo.setImgUrl(detail.getSkuDefaultImg());
        orderInfo.setOperateTime(new Date());
        orderInfo.setActivityReduceAmount(new BigDecimal("0"));
        // 秒杀优惠金额
        orderInfo.setCouponAmount(detail.getPrice().subtract(detail.getCostPrice()));
        orderInfo.setOriginalTotalAmount(detail.getCostPrice());
        orderInfo.setFeightFee(new BigDecimal("0"));
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setUserId(seckillOrderMsg.getUserId());
        orderDetail.setSkuId(seckillOrderMsg.getSkuId());
        orderDetail.setSkuName(detail.getSkuName());
        orderDetail.setImgUrl(detail.getSkuDefaultImg());
        orderDetail.setOrderPrice(detail.getCostPrice());
        orderDetail.setSkuNum(1);
        orderDetail.setCreateTime(new Date());
        orderDetail.setSplitTotalAmount(detail.getCostPrice());
        orderDetail.setSplitActivityAmount(new BigDecimal("0"));
        orderDetail.setSplitCouponAmount(detail.getPrice().subtract(detail.getCostPrice()));
        orderInfo.setOrderDetails(Collections.singletonList(orderDetail));

        // seckill:order:秒杀码
        redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDER + seckillOrderMsg.getCode(),
                JSON.toJSONString(orderInfo), 2, TimeUnit.DAYS);
    }

    @Override
    public void updateRedisStock(SeckillOrderMsg seckillOrderMsg) {
        Object json = redisTemplate.opsForHash().get(RedisConst.SECKILL_GOODS_CACHE + seckillOrderMsg.getDate(),
                seckillOrderMsg.getSkuId().toString());
        SeckillGoods goods = JSON.parseObject(json.toString(), SeckillGoods.class);
        goods.setStockCount(goods.getStockCount() - 1);
        redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS_CACHE + seckillOrderMsg.getDate(),
                seckillOrderMsg.getSkuId().toString(), JSON.toJSONString(goods));
    }
}
