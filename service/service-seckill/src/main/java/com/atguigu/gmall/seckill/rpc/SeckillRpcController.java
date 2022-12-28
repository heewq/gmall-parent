package com.atguigu.gmall.seckill.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.seckill.biz.SeckillBizService;
import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.seckill.vo.SeckillOrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/inner/rpc/seckill")
public class SeckillRpcController {
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private SeckillBizService seckillBizService;

    /**
     * 查询当天参与秒杀的所有商品
     *
     * @return
     */
    @GetMapping("/today/goods")
    public Result<List<SeckillGoods>> getTodaySeckillGoods() {
        String date = DateUtil.formatDate(new Date());
        List<SeckillGoods> goods = seckillGoodsService.getSeckillGoodsByDayFromCache(date);
        return Result.ok(goods);
    }

    @GetMapping("/detail/{skuId}")
    public Result<SeckillGoods> getSeckillGoodsDetail(@PathVariable Long skuId) {
        SeckillGoods seckillGoods = seckillGoodsService.getDetail(skuId);
        return Result.ok(seckillGoods);
    }

    /**
     * 获取秒杀单数据
     *
     * @param code
     * @return
     */
    @GetMapping("/order/detail/{code}")
    public Result<SeckillOrderConfirmVo> getSeckillOrderInfo(@PathVariable String code) {
        SeckillOrderConfirmVo orderConfirmVo = seckillBizService.getSeckillOrderInfo(code);
        return Result.ok(orderConfirmVo);
    }
}
