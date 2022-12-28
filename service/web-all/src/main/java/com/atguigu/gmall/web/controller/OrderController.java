package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.order.vo.OrderConfirmRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("/trade.html")
    public String trade(Model model) {

        OrderConfirmRespVo confirmData = orderFeignClient.orderConfirmData().getData();

        // {skuId, imgUrl, skuName, orderPrice, skuNum}
        model.addAttribute("detailArrayList", confirmData.getDetailArrayList());
        model.addAttribute("totalNum", confirmData.getTotalNum());
        model.addAttribute("totalAmount", confirmData.getTotalAmount());
        model.addAttribute("userAddressList", confirmData.getUserAddressList());
        model.addAttribute("tradeNo", confirmData.getTradeNum());
        return "order/trade";
    }

    @GetMapping("/myOrder.html")
    public String OrderListPage() {
        return "order/myOrder";
    }
}
