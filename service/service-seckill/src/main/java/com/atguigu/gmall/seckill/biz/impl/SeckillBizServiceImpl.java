package com.atguigu.gmall.seckill.biz.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.config.mq.MqService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.common.util.UserAuthUtil;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.mq.seckill.SeckillOrderMsg;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.seckill.biz.SeckillBizService;
import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.seckill.vo.SeckillOrderConfirmVo;
import com.atguigu.gmall.seckill.vo.SeckillOrderSubmitVo;
import com.atguigu.gmall.user.entity.UserAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SeckillBizServiceImpl implements SeckillBizService {
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MqService mqService;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    public String generateSeckillCode(Long skuId) {
        // 前置校验
        SeckillGoods detail = seckillGoodsService.getDetail(skuId);
        // - 开始时间校验
        Date current = new Date();
        if (!current.after(detail.getStartTime())) {
            // 秒杀还没开始
            throw new GmallException(ResultCodeEnum.SECKILL_NO_START);
        }
        // - 结束时间校验
        if (current.after(detail.getEndTime())) {
            // 秒杀已经结束
            throw new GmallException(ResultCodeEnum.SECKILL_END);
        }
        // - 校验库存[本地缓存]
        // 本地内存数据中没有库存 数据库中一定没有库存
        // 本地内存数据中有库存 数据库中不一定有库存
        if (detail.getStockCount() <= 0) {
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }

        // 允许参与后续秒杀 生成秒杀码
        String code = generateCode(skuId);
        // 缓存到redis 后续用于校验 防止秒杀脚本
        redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_CODE + code, "1", 2, TimeUnit.DAYS);
        return code;
    }

    @Override
    public void seckillOrder(Long skuId, String code) {
        // 合法性校验
        SeckillGoods detail = seckillGoodsService.getDetail(skuId);
        Date date = new Date();

        if (!date.after(detail.getStartTime())) {
            throw new GmallException(ResultCodeEnum.SECKILL_NO_START);
        }
        if (date.after(detail.getEndTime())) {
            throw new GmallException(ResultCodeEnum.SECKILL_END);
        }
        if (detail.getStockCount() <= 0) {
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }
        // 校验秒杀码
        String generateCode = generateCode(skuId);
        if (!generateCode.equals(code)) {
            throw new GmallException(ResultCodeEnum.SECKILL_ILLEGAL);
        }
        // redis
        if (Boolean.FALSE.equals(redisTemplate.hasKey(RedisConst.SECKILL_CODE + code))) {
            throw new GmallException(ResultCodeEnum.SECKILL_ILLEGAL);
        }

        // 统计相同秒杀请求的数量
        Long increment = redisTemplate.opsForValue().increment(RedisConst.SECKILL_CODE + code);
        // 合法请求 开始排队秒杀下单
        // 给mq发送排队消息 并且内存库存状态 -1
        detail.setStockCount(detail.getStockCount() - 1);
        if (increment <= 2) {
            SeckillOrderMsg seckillOrderMsg = new SeckillOrderMsg();
            seckillOrderMsg.setUserId(UserAuthUtil.getUserId());
            seckillOrderMsg.setCode(code);
            seckillOrderMsg.setSkuId(skuId);
            seckillOrderMsg.setDate(DateUtil.formatDate(new Date()));
            // 同一个用户同一个商品只能发送一次秒杀请求
            mqService.send(MqConst.SECKILL_EVENT_EXCHANGE, MqConst.SECKILL_ORDER_RK, seckillOrderMsg);
        } else {
            log.info("请求已经发过了......");
        }
    }

    @Override
    public ResultCodeEnum checkOrder(Long skuId) {
        String generateCode = generateCode(skuId);
        // 查询redis 找秒杀单数据 如果有则秒杀成功
        String json = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDER + generateCode);
        if ("x".equals(json)) {
            // 秒杀扣库存失败
            return ResultCodeEnum.SECKILL_FINISH;
        }

        if (!StringUtils.isEmpty(json)) {
            OrderInfo orderInfo = JSON.parseObject(json, OrderInfo.class);
            if (StringUtils.isEmpty(orderInfo.getDeliveryAddress())) {
                // 没有收货地址
                return ResultCodeEnum.SECKILL_SUCCESS;
            } else {
                return ResultCodeEnum.SECKILL_ORDER_SUCCESS;
            }
        }
        String count = redisTemplate.opsForValue().get(RedisConst.SECKILL_CODE + generateCode);
        if (Long.parseLong(count) > 1) {
            return ResultCodeEnum.SECKILL_RUN;
        }
        return ResultCodeEnum.SECKILL_FAIL;
    }

    @Override
    public SeckillOrderConfirmVo getSeckillOrderInfo(String code) {
        String json = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDER + code);
        OrderInfo orderInfo = JSON.parseObject(json, OrderInfo.class);
        SeckillOrderConfirmVo orderConfirmVo = new SeckillOrderConfirmVo();
        orderConfirmVo.setDetailArrayList(orderInfo.getOrderDetails());
        orderConfirmVo.setTotalNumber(1);
        orderConfirmVo.setTotalAmount(orderInfo.getTotalAmount());
        Long userId = UserAuthUtil.getUserId();
        List<UserAddress> userAddresses = userFeignClient.getUserAddresses(userId).getData();
        orderConfirmVo.setUserAddressList(userAddresses);
        return orderConfirmVo;
    }

    @Override
    public Long submitOrder(SeckillOrderSubmitVo submitVo) {
        // 获取秒杀码
        String code = submitVo.getCode();
        // 从redis拿到秒杀单数据
        String json = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDER + code);
        OrderInfo orderInfo = JSON.parseObject(json, OrderInfo.class);
        orderInfo.setConsignee(submitVo.getConsignee());
        orderInfo.setConsigneeTel(submitVo.getConsigneeTel());
        orderInfo.setDeliveryAddress(submitVo.getDeliveryAddress());
        orderInfo.setOrderComment(submitVo.getOrderComment());
        // 远程调用订单服务创建(保存)秒杀订单
        Long orderId = orderFeignClient.saveSeckillOrder(orderInfo).getData();
        orderInfo.setId(orderId);
        // 保存到redis
        redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDER + code, JSON.toJSONString(orderInfo));
        return orderId;
    }

    private String generateCode(Long skuId) {
        String date = DateUtil.formatDate(new Date());
        Long userId = UserAuthUtil.getUserId();
        String str = date + "_" + userId + "_" + skuId;
        return MD5.encrypt(str);
    }
}
