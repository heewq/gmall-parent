package com.atguigu.gmall.order.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.UserAuthUtil;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.vo.OrderConfirmRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inner/rpc/order")
public class OrderRpcController {
    @Autowired
    private OrderBizService orderBizService;
    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 获取订单确认页数据
     *
     * @return
     */
    @GetMapping("/confirmData")
    public Result<OrderConfirmRespVo> orderConfirmData() {
        OrderConfirmRespVo respVo = orderBizService.getConfirmData();
        return Result.ok(respVo);
    }

    @GetMapping("/orderInfo/{orderId}")
    public Result<OrderInfo> getOrderInfo(@PathVariable Long orderId) {
        // 分片键
        Long userId = UserAuthUtil.getUserId();
        OrderInfo orderInfo = orderInfoService.getByOrderIdAndUserId(orderId, userId);
        return Result.ok(orderInfo);
    }

    /**
     * 保存秒杀单
     *
     * @param orderInfo
     * @return
     */
    @PostMapping("/seckill/order")
    public Result<Long> saveSeckillOrder(@RequestBody OrderInfo orderInfo) {
        Long orderId = orderBizService.saveSeckillOrder(orderInfo);
        return Result.ok(orderId);
    }
}
