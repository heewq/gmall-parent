package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.seckill.biz.SeckillBizService;
import com.atguigu.gmall.seckill.vo.SeckillOrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity/seckill/auth")
public class SeckillApiController {
    @Autowired
    private SeckillBizService seckillBizService;

    /**
     * 生成秒杀码 用于控制后续所有秒杀流程
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillCode(@PathVariable Long skuId) {
        String code = seckillBizService.generateSeckillCode(skuId);
        return Result.ok(code);
    }


    /**
     * 下秒杀单
     *
     * @param skuId
     * @param code
     * @return
     */
    @PostMapping("/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId, @RequestParam("skuIdStr") String code) {

        seckillBizService.seckillOrder(skuId, code);

        return Result.ok();
    }

    /**
     * 检查秒杀单
     *
     * @param skuId
     * @return
     */
    @GetMapping("/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId) {
        // 检查秒杀单状态
        ResultCodeEnum codeEnum = seckillBizService.checkOrder(skuId);
        return Result.build("", codeEnum);
    }

    /**
     * 提交秒杀单
     *
     * @return
     */
    @PostMapping("/submitOrder")
    public Result submitOrder(@RequestBody SeckillOrderSubmitVo submitVo) {
        Long orderId = seckillBizService.submitOrder(submitVo);
        return Result.ok(orderId.toString());
    }
}
