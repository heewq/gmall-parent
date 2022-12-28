package com.atguigu.gmall.seckill.biz;

import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.seckill.vo.SeckillOrderConfirmVo;
import com.atguigu.gmall.seckill.vo.SeckillOrderSubmitVo;

public interface SeckillBizService {
    /**
     * 生成秒杀码
     *
     * @param skuId
     * @return
     */
    String generateSeckillCode(Long skuId);

    /**
     * 秒杀下单: 秒杀开始
     *
     * @param skuId
     * @param code
     */
    void seckillOrder(Long skuId, String code);

    /**
     * 检查面秒杀单
     *
     * @param skuId
     * @return
     */
    ResultCodeEnum checkOrder(Long skuId);

    /**
     * 获取秒杀但数据
     *
     * @param code
     * @return
     */
    SeckillOrderConfirmVo getSeckillOrderInfo(String code);

    /**
     * 提交秒杀单
     *
     * @param submitVo
     * @return
     */
    Long submitOrder(SeckillOrderSubmitVo submitVo);
}
