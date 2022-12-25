package com.atguigu.gmall.pay.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.util.UserAuthUtil;
import com.atguigu.gmall.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PayController {

    @Autowired
    private PayService payService;

    @GetMapping("/alipay/submit/{orderId}")
    public String alipay(@PathVariable Long orderId) throws AlipayApiException {
        Long userId = UserAuthUtil.getUserId();

        return payService.generatePayPage(orderId, userId);
    }
}
