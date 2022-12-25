package com.atguigu.gmall.order.biz.impl;

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
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.entity.OrderDetail;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.vo.OrderConfirmRespVo;
import com.atguigu.gmall.order.vo.OrderSubmitVo;
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
