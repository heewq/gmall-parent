package com.atguigu.gmall.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.pay.config.properties.AlipayProperties;
import com.atguigu.gmall.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayProperties alipayProperties;

    @Override
    public String generatePayPage(Long orderId, Long userId) throws AlipayApiException {

        // 创建支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        // 设置参数
        alipayRequest.setReturnUrl(alipayProperties.getReturn_url()); // 同步回调: 支付成功后浏览器会跳转到的页面地址
        alipayRequest.setNotifyUrl(alipayProperties.getNotify_url()); // 通知回调: 支付成功后支付消息会通知给这个地址

        // 准备待支付的订单数据
        // 远程调用订单服务获取订单基本数据 基于此数据构造支付页
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId).getData();

        //   - 商户订单号
        String outTradeNo = orderInfo.getOutTradeNo();
        //   - 付款金额
        BigDecimal totalAmount = orderInfo.getTotalAmount();
        //   - 订单名称
        String orderName = "尚品汇-订单-" + outTradeNo;
        //   - 商品描述
        String tradeBody = orderInfo.getTradeBody();

        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", totalAmount);
        bizContent.put("subject", orderName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("body", tradeBody);
        // 自动关单
        bizContent.put("time_expire", DateUtil.formatDate(orderInfo.getExpireTime(), "yyyy-MM-dd HH:mm:ss"));
        alipayRequest.setBizContent(JSON.toJSONString(bizContent));

        return alipayClient.pageExecute(alipayRequest).getBody();
    }
}
