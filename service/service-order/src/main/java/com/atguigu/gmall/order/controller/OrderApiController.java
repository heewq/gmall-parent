package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.vo.OrderSplitResp;
import com.atguigu.gmall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    @Autowired
    private OrderBizService orderBizService;

    @PostMapping("/orderSplit")
    public List<OrderSplitResp> orderSplit(@RequestParam Long orderId,
                                           @RequestParam("wareSkuMap") String json) {
        return orderBizService.orderSplit(orderId, json);
    }

    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestParam String tradeNo,
                              @Valid @RequestBody OrderSubmitVo submitVo) {
        // 下单
        Long orderId = orderBizService.submitOrder(tradeNo, submitVo);
        // 以字符串类型返回
        return Result.ok(orderId.toString());
    }
}
