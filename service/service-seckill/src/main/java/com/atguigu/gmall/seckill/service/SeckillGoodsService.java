package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.mq.seckill.SeckillOrderMsg;
import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【seckill_goods】的数据库操作Service
 * @createDate 2022-12-27 20:16:44
 */
public interface SeckillGoodsService extends IService<SeckillGoods> {

    /**
     * 查询指定日期参与秒杀的所有商品
     *
     * @param date
     * @return
     */
    List<SeckillGoods> getSeckillGoodsByDay(String date);

    List<SeckillGoods> getSeckillGoodsByDayFromCache(String date);

    void saveToLocalCache(List<SeckillGoods> goods);

    SeckillGoods getDetail(Long skuId);

    /**
     * 扣库存
     *
     * @param id
     */
    void deduceStock(Long id);

    /**
     * 临时保存秒杀单
     *
     * @param seckillOrderMsg
     */
    void saveSeckillOrder(SeckillOrderMsg seckillOrderMsg);


    /**
     * 更新redis中的库存数据
     *
     * @param seckillOrderMsg
     */
    void updateRedisStock(SeckillOrderMsg seckillOrderMsg);
}
