package com.atguigu.gmall.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.mq.seckill.SeckillOrderMsg;
import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 监听秒杀排队请求 下秒杀单
 */
@Service
@Slf4j
public class SeckillOrderListener {
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = MqConst.SECKILL_ORDER_QUEUE, durable = "true", exclusive = "false", autoDelete = "false"),
                    exchange = @Exchange(value = MqConst.SECKILL_EVENT_EXCHANGE, durable = "true", autoDelete = "false", type = "topic"),
                    key = MqConst.SECKILL_ORDER_RK
            )
    })
    public void listen(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String content = new String(message.getBody());
        SeckillOrderMsg seckillOrderMsg = JSON.parseObject(content, SeckillOrderMsg.class);
        log.info("秒杀单排队请求: {}", seckillOrderMsg);
        // 下秒杀单
        SeckillGoods detail = seckillGoodsService.getDetail(seckillOrderMsg.getSkuId());
        try {
            // 数据库扣库存
            seckillGoodsService.deduceStock(detail.getId());

            // 扣库存成功 redis中保存一个临时秒杀单数据
            log.info("用户: {} 商品: {} 秒杀成功", seckillOrderMsg.getUserId(), seckillOrderMsg.getSkuId());
            seckillGoodsService.saveSeckillOrder(seckillOrderMsg);
            // 更新redis中的库存数据
            seckillGoodsService.updateRedisStock(seckillOrderMsg);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 扣库存异常 说明库存没有了
            // 在redis保存一个占位符
            redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDER + seckillOrderMsg.getCode(),
                    "x", 2, TimeUnit.DAYS);
            log.warn("用户: {} 商品: {} 秒杀失败", seckillOrderMsg.getUserId(), seckillOrderMsg.getSkuId());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
