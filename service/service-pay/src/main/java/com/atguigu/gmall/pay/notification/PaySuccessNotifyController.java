package com.atguigu.gmall.pay.notification;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.config.mq.MqService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.pay.config.properties.AlipayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequestMapping("/api/payment")
@RestController
public class PaySuccessNotifyController {
    @Autowired
    private AlipayProperties alipayProperties;
    @Autowired
    private MqService mqService;

    /**
     * 接收支付成功后支付宝的支付结果通知
     *
     * @param params
     * @return
     */
    @PostMapping("/notify/success")
    public String paySuccessNotify(@RequestParam Map<String, String> params) throws AlipayApiException {
        log.info("收到支付宝通知消息: {}", JSON.toJSONString(params));
        // verify signature
        boolean signVerified = AlipaySignature.rsaCheckV1(params,
                alipayProperties.getAlipay_public_key(),
                alipayProperties.getCharset(),
                alipayProperties.getSign_type());
        if (!signVerified) {
            log.error("error signature");
            throw new GmallException(ResultCodeEnum.INVALID_SIGNATURE);
        }
        String tradeStatus = params.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // 验签通过 支付状态为 TRADE_SUCCESS 发修改订单状态消息
            mqService.send(MqConst.ORDER_EVENT_EXCHANGE, MqConst.ORDER_PAID_RK, params);
        }

        return "success";
    }
}
