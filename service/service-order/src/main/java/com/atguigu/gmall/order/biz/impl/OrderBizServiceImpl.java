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
                    // ????????????
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

        // ????????????????????????
        // ????????????????????????
        String tradeNo = "ATGUIGU-" + System.currentTimeMillis() + "-" + userId;
        respVo.setTradeNum(tradeNo);
        // redis
        redisTemplate.opsForValue().set(RedisConst.REPEAT_TOKEN + tradeNo, "1", 5, TimeUnit.MINUTES);

        return respVo;
    }

    @Override
    @Transactional
    public Long submitOrder(String tradeNo, OrderSubmitVo submitVo) {
        // ???????????? JSR303
        // ????????????
        //   - ??????
        Boolean bool = redisTemplate.delete(RedisConst.REPEAT_TOKEN + tradeNo);
        if (Boolean.FALSE.equals(bool)) {
            throw new GmallException(ResultCodeEnum.REPEAT_REQUEST);
        }

        //   - ??????
        List<OrderSubmitVo.OrderDetailListDTO> noStockSkus = submitVo.getOrderDetailList()
                .stream()
                .filter(orderDetail -> "0".equals(wareFeignClient.hasStock(orderDetail.getSkuId(), orderDetail.getSkuNum())))
                .collect(Collectors.toList());
        if (noStockSkus.size() > 0) {
            String skuNames = noStockSkus.stream()
                    .map(OrderSubmitVo.OrderDetailListDTO::getSkuName)
                    .reduce((o1, o2) -> o1 + ";" + o2)
                    .get();
            throw new GmallException(skuNames + "; ????????????", ResultCodeEnum.NO_STOCK.getCode());
        }

        //   - ??????
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
            throw new GmallException(skuNames + ";  ??????????????????, ???????????????????????????", ResultCodeEnum.PRICE_CHANGED.getCode());
        }

        // ??????????????????
        //   - order_info
        OrderInfo orderInfo = prepareOrderInfo(submitVo, tradeNo);
        orderInfoService.save(orderInfo);
        Long orderId = orderInfo.getId();

        // - order_detail
        List<OrderDetail> orderDetails = prepareOrderDetails(submitVo, orderInfo);
        orderDetailService.saveBatch(orderDetails);

        // ?????????????????????????????????MQ
        mqService.send(MqConst.ORDER_EVENT_EXCHANGE, MqConst.ORDER_CREAT_RK, orderInfo);

        // ???????????? ???????????????????????????
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
        log.info("??????: {} ??????: {}", orderId, update);
    }

    @Override
    public void paidOrder(String outTradeNo, Long userId) {
        // ??????????????????????????????????????? ???????????????????????????
        // ??????????????? ??????????????? ?????????????????????????????????
        // ??????????????? ??????????????? ??????????????????????????????
        ProcessStatus paid = ProcessStatus.PAID;
        boolean update = orderInfoService.lambdaUpdate()
                .set(OrderInfo::getOrderStatus, paid.getOrderStatus().name())
                .set(OrderInfo::getProcessStatus, paid.name())
                .eq(OrderInfo::getOutTradeNo, outTradeNo)
                .eq(OrderInfo::getUserId, userId)
                .in(OrderInfo::getOrderStatus, OrderStatus.UNPAID.name(), OrderStatus.CLOSED.name())
                .in(OrderInfo::getProcessStatus, ProcessStatus.UNPAID.name(), ProcessStatus.CLOSED.name())
                .update();
        log.info("??????: {} ?????????: {}", outTradeNo, update);
    }

    @Override
    public void updateOrderStatus(WareStockResultMsg resultMsg) {
        // ??????????????????????????????????????????????????????
        ProcessStatus status;
        if (resultMsg.getStatus().equals("OUT_OF_STOCK")) {
            status = ProcessStatus.STOCK_EXCEPTION;
        } else {
            status = ProcessStatus.WAITING_DELEVER;
        }
        OrderInfo orderInfo = orderInfoService.getById(resultMsg.getOrderId());
        // ??????
        boolean update = orderInfoService.lambdaUpdate()
                .set(OrderInfo::getOrderStatus, status.getOrderStatus().name())
                .set(OrderInfo::getProcessStatus, status.name())
                .eq(OrderInfo::getId, orderInfo.getId())
                .eq(OrderInfo::getUserId, orderInfo.getUserId())
                .eq(OrderInfo::getOrderStatus, OrderStatus.PAID.name())
                .eq(OrderInfo::getProcessStatus, ProcessStatus.PAID.name())
                .update();
        log.info("??????????????????????????????");

        // ??????????????? ????????????
        if ("DEDUCTED".equals(resultMsg.getStatus())) {
            OrderLogisticMsg logisticMsg = new OrderLogisticMsg();
            logisticMsg.setOrderId(orderInfo.getId());
            logisticMsg.setUserId(orderInfo.getUserId());
            // ????????????????????????????????????????????????
            mqService.send(MqConst.ORDER_EVENT_EXCHANGE, MqConst.ORDER_LOGISTIC_RK, logisticMsg);
        }
    }

    @Override
    public List<OrderSplitResp> orderSplit(Long orderId, String json) {
        //[{"wareId":"1","skuIds":["53"]},{"wareId":"2","skuIds":["52"]}]

        OrderInfo parentOrder = orderInfoService.getById(orderId);

        // ?????????????????????????????????????????????
        List<SkuWare> skuWares = JSON.parseObject(json, new TypeReference<List<SkuWare>>() {
        });

        AtomicInteger subNum = new AtomicInteger(0);
        // ???????????????
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

                    // ??????????????????
                    childOrder.setWareId(skuWare.getWareId());

                    return childOrder;
                }).collect(Collectors.toList());
        for (OrderInfo orderInfo : childOrders) {
            // ???????????????
            orderInfoService.save(orderInfo);
            Long childId = orderInfo.getId();

            // ?????????????????????
            List<OrderDetail> orderDetails = orderInfo.getOrderDetails()
                    .stream()
                    .peek(orderDetail -> orderDetail.setOrderId(childId))
                    .collect(Collectors.toList());
            orderDetailService.saveBatch(orderDetails);
        }

        // ?????????????????????????????????
        boolean update = orderInfoService.lambdaUpdate()
                .set(OrderInfo::getOrderStatus, OrderStatus.SPLIT.name())
                .set(OrderInfo::getProcessStatus, ProcessStatus.SPLIT.name())
                .eq(OrderInfo::getId, parentOrder.getId())
                .eq(OrderInfo::getUserId, parentOrder.getUserId())
                .update();

        log.info("????????????: {}", parentOrder.getId());

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

    @Override
    public Long saveSeckillOrder(OrderInfo orderInfo) {
        // ??????????????????
        boolean save = orderInfoService.save(orderInfo);
        // ??????????????????
        List<OrderDetail> orderDetails = orderInfo.getOrderDetails()
                .stream()
                .peek(orderDetail -> {
                    orderDetail.setOrderId(orderInfo.getId()); // ?????? Id
                }).collect(Collectors.toList());
        orderDetailService.saveBatch(orderDetails);

        // todo ??????mq??????????????????????????????

        return orderInfo.getId();
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
        // ???????????? = ???????????? - ????????????
        BigDecimal totalAmount = submitVo.getOrderDetailList()
                .stream()
                .map(orderDetail -> orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())))
                .reduce(BigDecimal::add)
                .get();
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());

        orderInfo.setUserId(UserAuthUtil.getUserId());
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());
        // ???????????????
        orderInfo.setOutTradeNo(tradeNo);
        // ?????????
        String skuName = submitVo.getOrderDetailList().get(0).getSkuName();
        orderInfo.setTradeBody(skuName);
        orderInfo.setCreateTime(new Date());
        // ????????????: 30min ????????? ????????????
        Date expireDate = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
        orderInfo.setExpireTime(expireDate);
        // ????????????
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());

        String imgUrl = submitVo.getOrderDetailList().get(0).getImgUrl();
        orderInfo.setImgUrl(imgUrl);
        orderInfo.setOperateTime(new Date());
        // ????????????
        orderInfo.setOriginalTotalAmount(totalAmount);

        return orderInfo;
    }
}
