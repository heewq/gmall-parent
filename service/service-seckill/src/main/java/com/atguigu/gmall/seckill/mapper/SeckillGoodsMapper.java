package com.atguigu.gmall.seckill.mapper;

import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author vcwfhe
 * @description 针对表【seckill_goods】的数据库操作Mapper
 * @createDate 2022-12-27 20:16:44
 * @Entity com.atguigu.gmall.seckill.entity.SeckillGoods
 */
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    List<SeckillGoods> getSeckillGoodsByDay(@Param("date") String date);

    void updateStock(@Param("id") Long id);
}
