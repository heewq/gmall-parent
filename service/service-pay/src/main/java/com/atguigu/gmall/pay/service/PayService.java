package com.atguigu.gmall.pay.service;

import com.alipay.api.AlipayApiException;

public interface PayService {
    /**
     * 生成支付页
     *
     * @param orderId
     * @param userId
     * @return
     */
    String generatePayPage(Long orderId, Long userId) throws AlipayApiException;
}
