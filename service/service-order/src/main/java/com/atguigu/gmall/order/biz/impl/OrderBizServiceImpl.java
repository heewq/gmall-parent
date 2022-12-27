package com.atguigu.gmall.order.biz.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gmall.common.config.mq.MqService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.UserAuthUtil;
import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.PaymentWay;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.feign.product.ProductSkuDetailFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.feign.ware.WareFeignClient;
import com.atguigu.gmall.mq.logistic.OrderLogisticMsg;
import com.atguigu.gmall.mq.ware.WareStockResultMsg;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.entity.OrderDetail;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.vo.OrderConfirmRespVo;
import com.atguigu.gmall.order.vo.OrderSplitResp;
import com.atguigu.gmall.order.vo.OrderSubmitVo;
import com.atguigu.gmall.order.vo.SkuWare;
import com.atguigu.gmall.user.entity.UserAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderBizServiceImpl implements OrderBizService {
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private ProductSkuDetailFeignClient skuDetailFeignClient;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private WareFeignClient wareFeignClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private MqService mqService;

    @Override
    public OrderConfirmRespVo getConfirmData() {
        OrderConfirmRespVo respVo = new OrderConfirmRespVo();
        List<OrderConfirmRespVo.SkuDetail> skuDetails = cartFeignClient.getChecked().getData()
                .stream()
                .map(cartInfo -> {
                    OrderConfirmRespVo.SkuDetail skuDetail = new OrderConfirmRespVo.SkuDetail();
                    skuDetail.setSkuNum(cartInfo.getSkuNum());
                    skuDetail.setSkuId(cartInfo.getSkuId());
                    skuDetail.setImgUrl(cartInfo.getImgUrl());
                    skuDetail.setSkuName(cartInfo.getSkuName());
                    // 实时价格
                    BigDecimal price = skuDetailFeignClient.getPrice(cartInfo.getSkuId()).getData();
                    skuDetail.setOrderPrice(price);

                    String hasStock = wareFeignClient.hasStock(skuDetail.getSkuId(), skuDetail.getSkuNum());
                    skuDetail.setHasStock(hasStock);
                    return skuDetail;
                }).collect(Collectors.toList());
        respVo.setDetailArrayList(skuDetails);

        Integer totalNum = skuDetails.stream()
                .map(OrderConfirmRespVo.SkuDetail::getSkuNum)
                .reduce(Integer::sum)
                .get();
        respVo.setTotalNum(totalNum);

        BigDecimal totalAmount = skuDetails.stream()
                .map(skuDetail -> skuDetail.getOrderPrice().multiply(new BigDecimal(skuDetail.getSkuNum())))
                .reduce(BigDecimal::add)
                .get();
        respVo.setTotalAmount(totalAmount);

        Long userId = UserAuthUtil.getUserId();
        List<UserAddress> addresses = userFeignClient.getUserAddresses(userId).getData();
        respVo.setUserAddressList(addresses);

        // 开启订单追踪功能
        // 防止订单重复提交
        String tradeNo = "ATGUIGU-" + System.currentTimeMillis() + "-" + userId;
        respVo.setTradeNum(tradeNo);
        // redis
        redisTemplate.opsForValue().set(RedisConst.REPEAT_TOKEN + tradeNo, "1", 5, TimeUnit.MINUTES);

        return respVo;
    }

    @Override
    @Transactional
    public Long submitOrder(String tradeNo, OrderSubmitVo submitVo) {
        // 参数校验 JSR303
        // 业务校验
        //   - 令牌
        Boolean bool = redisTemplate.delete(RedisConst.REPEAT_TOKEN + tradeNo);
        if (Boolean.FALSE.equals(bool)) {
            throw new GmallException(ResultCodeEnum.REPEAT_REQUEST);
        }

        //   - 库存
        List<OrderSubmitVo.OrderDetailListDTO> noStockSkus = submitVo.getOrderDetailList()
                .stream()
                .filter(orderDetail -> "0".equals(wareFeignClient.hasStock(orderDetail.getSkuId(), orderDetail.getSkuNum())))
                .collect(Collectors.toList());
        if (noStockSkus.size() > 0) {
            String skuNames = noStockSkus.stream()
                    .map(OrderSubmitVo.OrderDetailListDTO::getSkuName)
                    .reduce((o1, o2) -> o1 + ";" + o2)
                    .get();
            throw new GmallException(skuNames + "; 没有库存", ResultCodeEnum.NO_STOCK.getCode());
        }

        //   - 价格
        List<OrderSubmitVo.OrderDetailListDTO> priceChangedSkus = submitVo.getOrderDetailList()
                .stream()
                .filter(orderDetail -> {
                    BigDecimal orderPrice = orderDetail.getOrderPrice();
                    BigDecimal price = skuDetailFeignClient.getPrice(orderDetail.getSkuId()).getData();
                    return Math.abs(price.subtract(orderPrice).doubleValue()) >= 0.001;
                }).collect(Collectors.toList());
        if (priceChangedSkus.size() > 0) {
            String skuNames = priceChangedSkus.stream()
                    .map(OrderSubmitVo.OrderDetailListDTO::getSkuName)
                    .reduce((o1, o2) -> o1 + ";" + o2)
                    .get();
            throw new GmallException(skuNames + ";  商品价格变化, 请刷新页面重新确认", ResultCodeEnum.PRICE_CHANGED.getCode());
        }

        // 保存到数据库
        //   - order_info
        OrderInfo orderInfo = prepareOrderInfo(submitVo, tradeNo);
        orderInfoService.save(orderInfo);
        Long orderId = orderInfo.getId();

        // - order_detail
        List<OrderDetail> orderDetails = prepareOrderDetails(submitVo, orderInfo);
        orderDetailService.saveBatch(orderDetails);

        // 发送订单创建成功消息到MQ
        mqService.send(MqConst.ORDER_EVENT_EXCHANGE, MqConst.ORDER_CREAT_RK, orderInfo);

        // 提交成功 删除购物车中的数据
        cartFeignClient.deleteChecked();
        return orderId;
    }

    @Override
    public void closeOrder(Long orderId, Long userId) {
        // process_status = CLOSE
        // order_status = CLOSE
        ProcessStatus closed = ProcessStatus.CLOSED;
        boolean update = orderInfoService.lambdaUpdate()
                .set(OrderInfo::getOrderStatus, closed.getOrderStatus().name())
                .set(OrderInfo::getProcessStatus, closed.name())
                .eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOrderStatus, OrderStatus.UNPAID.name())
                .eq(OrderInfo::getProcessStatus, ProcessStatus.UNPAID.name())
                .update();
        log.info("订单: {} 关闭: {}", orderId, update);
    }

    @Override
    public void paidOrder(String outTradeNo, Long userId) {
        // 关单消息和支付消息同时抵达 必须以支付状态为准
        // 关单先运行 改为已关闭 支付运行后应改为已支付
        // 支付先运行 改为已支付 关单后运行什么都不做
        ProcessStatus paid = ProcessStatus.PAID;
        boolean update = orderInfoService.lambdaUpdate()
                .set(OrderInfo::getOrderStatus, paid.getOrderStatus().name())
                .set(OrderInfo::getProcessStatus, paid.name())
                .eq(OrderInfo::getOutTradeNo, outTradeNo)
                .eq(OrderInfo::getUserId, userId)
                .in(OrderInfo::getOrderStatus, OrderStatus.UNPAID.name(), OrderStatus.CLOSED.name())
                .in(OrderInfo::getProcessStatus, ProcessStatus.UNPAID.name(), ProcessStatus.CLOSED.name())
                .update();
        log.info("修改: {} 已支付: {}", outTradeNo, update);
    }

    @Override
    public void updateOrderStatus(WareStockResultMsg resultMsg) {
        // 根据库存扣减结果决定订单要修改的状态
        ProcessStatus status;
        if (resultMsg.getStatus().equals("OUT_OF_STOCK")) {
            status = ProcessStatus.STOCK_EXCEPTION;
        } else {
            status = ProcessStatus.WAITING_DELEVER;
        }
        OrderInfo orderInfo = orderInfoService.getById(resultMsg.getOrderId());
        // 修改
        boolean update = orderInfoService.lambdaUpdate()
                .set(OrderInfo::getOrderStatus, status.getOrderStatus().name())
                .set(OrderInfo::getProcessStatus, status.name())
                .eq(OrderInfo::getId, orderInfo.getId())
                .eq(OrderInfo::getUserId, orderInfo.getUserId())
                .eq(OrderInfo::getOrderStatus, OrderStatus.PAID.name())
                .eq(OrderInfo::getProcessStatus, ProcessStatus.PAID.name())
                .update();
        log.info("订单库存状态更新完成");

        // 下电子面单 进行发货
        if ("DEDUCTED".equals(resultMsg.getStatus())) {
            OrderLogisticMsg logisticMsg = new OrderLogisticMsg();
            logisticMsg.setOrderId(orderInfo.getId());
            logisticMsg.setUserId(orderInfo.getUserId());
            // 给等待物流配送的订单队列发送消息
            mqService.send(MqConst.ORDER_EVENT_EXCHANGE, MqConst.ORDER_LOGISTIC_RK, logisticMsg);
        }
    }

    @Override
    public List<OrderSplitResp> orderSplit(Long orderId, String json) {
        //[{"wareId":"1","skuIds":["53"]},{"wareId":"2","skuIds":["52"]}]

        OrderInfo parentOrder = orderInfoService.getById(orderId);

        // 得到大订单中所有商品的库存分布
        List<SkuWare> skuWares = JSON.parseObject(json, new TypeReference<List<SkuWare>>() {
        });

        AtomicInteger subNum = new AtomicInteger(0);
        // 拆分子订单
        List<OrderInfo> childOrders = skuWares
                .stream()
                .map(skuWare -> {
                    OrderInfo childOrder = new OrderInfo();
                    childOrder.setConsignee(parentOrder.getConsignee());
                    childOrder.setConsigneeTel(parentOrder.getConsigneeTel());

                    List<Long> skuIds = skuWare.getSkuIds();

                    List<OrderDetail> orderDetails = orderDetailService.lambdaQuery()
                            .eq(OrderDetail::getOrderId, orderId)
                            .eq(OrderDetail::getUserId, parentOrder.getUserId())
                            .list();
                    List<OrderDetail> childDetails = orderDetails.stream()
                            .filter(orderDetail -> skuIds.contains(orderDetail.getSkuId()))
                            .collect(Collectors.toList());
                    childOrder.setOrderDetails(childDetails);
                    BigDecimal totalAmount = childDetails.stream()
                            .map(orderDetail -> orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())))
                            .reduce(BigDecimal::add)
                            .get();
                    childOrder.setTotalAmount(totalAmount);
                    childOrder.setOrderStatus(parentOrder.getOrderStatus());
                    childOrder.setUserId(parentOrder.getUserId());
                    childOrder.setPaymentWay(parentOrder.getPaymentWay());
                    childOrder.setDeliveryAddress(parentOrder.getDeliveryAddress());
                    childOrder.setOrderComment(parentOrder.getOrderComment());
                    childOrder.setOutTradeNo(subNum.getAndIncrement() + "_" + parentOrder.getOutTradeNo());
                    childOrder.setTradeBody(childDetails.get(0).getSkuName());
                    childOrder.setCreateTime(new Date());
                    childOrder.setExpireTime(parentOrder.getExpireTime());
                    childOrder.setProcessStatus(parentOrder.getProcessStatus());
                    childOrder.setParentOrderId(parentOrder.getId());
                    childOrder.setImgUrl(childDetails.get(0).getImgUrl());
                    childOrder.setOperateTime(new Date());
                    childOrder.setOriginalTotalAmount(totalAmount);

                    // 准备返回数据
                    childOrder.setWareId(skuWare.getWareId());

                    return childOrder;
                }).collect(Collectors.toList());
        for (OrderInfo orderInfo : childOrders) {
            // 保存子订单
            orderInfoService.save(orderInfo);
            Long childId = orderInfo.getId();

            // 保存子订单明细
            List<OrderDetail> orderDetails = orderInfo.getOrderDetails()
                    .stream()
                    .peek(orderDetail -> orderDetail.setOrderId(childId))
                    .collect(Collectors.toList());
            orderDetailService.saveBatch(orderDetails);
        }

        // 更改父订单状态为已拆分
        boolean update = orderInfoService.lambdaUpdate()
                .set(OrderInfo::getOrderStatus, OrderStatus.SPLIT.name())
                .set(OrderInfo::getProcessStatus, ProcessStatus.SPLIT.name())
                .eq(OrderInfo::getId, parentOrder.getId())
                .eq(OrderInfo::getUserId, parentOrder.getUserId())
                .update();

        log.info("拆单完成: {}", parentOrder.getId());

        return childOrders.stream()
                .map(orderInfo -> {
                    OrderSplitResp orderSplitResp = new OrderSplitResp();
                    orderSplitResp.setOrderId(orderInfo.getId());
                    orderSplitResp.setUserId(orderInfo.getUserId());
                    orderSplitResp.setConsignee(orderInfo.getConsignee());
                    orderSplitResp.setConsigneeTel(orderInfo.getConsigneeTel());
                    orderSplitResp.setOrderComment(orderInfo.getOrderComment());
                    orderSplitResp.setOrderBody(orderInfo.getTradeBody());
                    orderSplitResp.setDeliveryAddress(orderInfo.getDeliveryAddress());
                    orderSplitResp.setPaymentWay("2");
                    orderSplitResp.setWareId(orderInfo.getWareId());

                    List<OrderSplitResp.OrderSku> orderSkus = orderInfo.getOrderDetails()
                            .stream()
                            .map(orderDetail -> {
                                OrderSplitResp.OrderSku orderSku = new OrderSplitResp.OrderSku();
                                orderSku.setSkuId(orderDetail.getSkuId());
                                orderSku.setSkuNum(orderDetail.getSkuNum());
                                orderSku.setSkuName(orderDetail.getSkuName());
                                return orderSku;
                            }).collect(Collectors.toList());
                    orderSplitResp.setDetails(orderSkus);
                    return orderSplitResp;
                }).collect(Collectors.toList());
    }

    private List<OrderDetail> prepareOrderDetails(OrderSubmitVo submitVo, OrderInfo orderInfo) {
        return submitVo.getOrderDetailList()
                .stream()
                .map(orderDetail -> {
                    OrderDetail newOrderDetail = new OrderDetail();
                    newOrderDetail.setOrderId(orderInfo.getId());
                    newOrderDetail.setUserId(orderInfo.getUserId());
                    BeanUtils.copyProperties(orderDetail, newOrderDetail);
                    newOrderDetail.setCreateTime(new Date());
                    newOrderDetail.setSplitTotalAmount(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
                    return newOrderDetail;
                }).collect(Collectors.toList());
    }

    private OrderInfo prepareOrderInfo(OrderSubmitVo submitVo, String tradeNo) {
        OrderInfo orderInfo = new OrderInfo();

        BeanUtils.copyProperties(submitVo, orderInfo);
        // 订单总额 = 原价金额 - 优惠金额
        BigDecimal totalAmount = submitVo.getOrderDetailList()
                .stream()
                .map(orderDetail -> orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())))
                .reduce(BigDecimal::add)
                .get();
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());

        orderInfo.setUserId(UserAuthUtil.getUserId());
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());
        // 对外流水号
        orderInfo.setOutTradeNo(tradeNo);
        // 交易体
        String skuName = submitVo.getOrderDetailList().get(0).getSkuName();
        orderInfo.setTradeBody(skuName);
        orderInfo.setCreateTime(new Date());
        // 失效时间: 30min 不支付 订单关闭
        Date expireDate = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
        orderInfo.setExpireTime(expireDate);
        // 处理状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());

        String imgUrl = submitVo.getOrderDetailList().get(0).getImgUrl();
        orderInfo.setImgUrl(imgUrl);
        orderInfo.setOperateTime(new Date());
        // 原价金额
        orderInfo.setOriginalTotalAmount(totalAmount);

        return orderInfo;
    }
}
