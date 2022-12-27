package com.atguigu.gmall.order.biz;

import com.alibaba.fastjson.JSONObject;

public interface LogisticService {
    /**
     * 生成电子面单
     *
     * @param orderId
     * @param userId
     * @return
     */
    JSONObject generateEOrder(Long orderId, Long userId) throws Exception;

    JSONObject searchLogisticStatus();
}
