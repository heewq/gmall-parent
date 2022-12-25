package com.atguigu.gmall.feign.order;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.order.vo.OrderConfirmRespVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("service-order")
@RequestMapping("/api/inner/rpc/order")
public interface OrderFeignClient {
    @GetMapping("/confirmData")
    Result<OrderConfirmRespVo> orderConfirmData();

    @GetMapping("/orderInfo/{orderId}")
    Result<OrderInfo> getOrderInfo(@PathVariable Long orderId);
}
