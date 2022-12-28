package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.feign.seckill.SeckillFeignClient;
import com.atguigu.gmall.seckill.entity.SeckillGoods;
import com.atguigu.gmall.seckill.vo.SeckillOrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SeckillController {
    @Autowired
    private SeckillFeignClient seckillFeignClient;

    @GetMapping("/seckill.html")
    public String seckillPage(Model model) {

        // {skuId, skuDefaultImg, skuName, costPrice, price, num, stockCount}
        List<SeckillGoods> goods = seckillFeignClient.getTodaySeckillGoods().getData();
        model.addAttribute("list", goods);
        return "seckill/index";
    }

    /**
     * 秒杀商品详情
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/seckill/{skuId}.html")
    public String seckillDetail(@PathVariable Long skuId, Model model) {
        SeckillGoods seckillGoods = seckillFeignClient.getSeckillGoodsDetail(skuId).getData();
        model.addAttribute("item", seckillGoods);
        return "seckill/item";
    }

    /**
     * 秒杀排队页
     *
     * @param skuId
     * @param skuIdStr
     * @return
     */
    @GetMapping("/seckill/queue.html")
    public String queuePage(@RequestParam Long skuId, @RequestParam String skuIdStr, Model model) {

        model.addAttribute("skuId", skuId);
        model.addAttribute("skuIdStr", skuIdStr);
        return "seckill/queue";
    }

    /**
     * 秒杀订单确认页
     *
     * @param model
     * @return
     */
    @GetMapping("/seckill/trade.html")
    public String tradePage(Model model, @RequestParam("code") String code) {
        // 远程调用秒杀服务获取订单确认页数据
        SeckillOrderConfirmVo confirmVo = seckillFeignClient.getSeckillOrderInfo(code).getData();
        // 商品清单
        model.addAttribute("detailArrayList", confirmVo.getDetailArrayList());
        model.addAttribute("totalNum", confirmVo.getTotalNumber());
        model.addAttribute("totalAmount", confirmVo.getTotalAmount());
        model.addAttribute("userAddressList", confirmVo.getUserAddressList());
        return "seckill/trade";
    }
}
