package com.atguigu.gmall.common.constant;

public class MqConst {
    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";
    public static final String ORDER_DELAY_QUEUE = "order-delay-queue";
    public static final String ORDER_TIMEOUT_RK = "order.timeout";
    public static final String ORDER_CREAT_RK = "order.creat";
    public static final String ORDER_DEAD_QUEUE = "order-dead-queue";
    public static final Long ORDER_TTL = 30 * 60 * 1000L;
    public static final String ORDER_PAID_RK = "order.paid";
    public static final String ORDER_PAID_QUEUE = "order-paid_queue";
}
