package com.atguigu.gmall.feign.seckill;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.atguigu.gmall.seckill.vo.SeckillOrderConfirmVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("service-seckill")
@RequestMapping("/api/inner/rpc/seckill")
public interface SeckillFeignClient {
    /**
     * 查询当天参与秒杀的所有商品
     *
     * @return
     */
    @GetMapping("/today/goods")
    Result<List<SeckillGoods>> getTodaySeckillGoods();

    /**
     * 查询秒杀商品详情
     *
     * @param skuId
     * @return
     */
    @GetMapping("/detail/{skuId}")
    Result<SeckillGoods> getSeckillGoodsDetail(@PathVariable Long skuId);

    /**
     * 获取秒杀单数据
     *
     * @param code
     * @return
     */
    @GetMapping("/order/detail/{code}")
    Result<SeckillOrderConfirmVo> getSeckillOrderInfo(@PathVariable String code);
}
