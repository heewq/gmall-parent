package com.atguigu.gmall.order.biz.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.order.biz.LogisticService;
import com.atguigu.gmall.order.entity.OrderDetail;
import com.atguigu.gmall.order.entity.OrderInfo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LogisticServiceImpl implements LogisticService {
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private OrderDetailService orderDetailService;

    //用户ID，快递鸟提供，注意保管，不要泄漏
    private final String EBusinessID = "test1785709";//即用户ID，登录快递鸟官网会员中心获取 https://www.kdniao.com/UserCenter/v4/UserHome.aspx
    //API key，快递鸟提供，注意保管，不要泄漏
    private final String ApiKey = "d93b8ab4-2339-45be-915c-61a22af03157";//即API key，登录快递鸟官网会员中心获取 https://www.kdniao.com/UserCenter/v4/UserHome.aspx
    //请求url, 正式环境地址
    private final String ReqURL = "http://sandboxapi.kdniao.com:8080/kdniaosandbox/gateway/exterfaceInvoke.json";

    @Override
    public JSONObject generateEOrder(Long orderId, Long userId) throws Exception {
//        EOrderRequestData data = new EOrderRequestData();
//        data.setOrderCode("" + orderId);
//        data.setShipperCode("YTO");
//        data.setCustomerName("");
//        data.setCustomerPwd("");
//        data.setMonthCode("1234567890");
//        data.setSendSite("");
//        data.setPayType(1);
//        data.setExpType("1");
//        data.setCost(new BigDecimal("0"));
//        data.setOtherCost(new BigDecimal("0"));
//
//        // 发货人
//        Sender sender = new Sender();
//        sender.setCompany("尚品汇");
//        sender.setName("尚品汇中心仓库");
//        sender.setMobile("13888888888");
//        sender.setProvinceName("上海");
//        sender.setCityName("上海市");
//        sender.setExpAreaName("青浦区");
//        sender.setAddress("明珠路");
//        data.setSender(sender);
//
//        // 收货人
        OrderInfo orderInfo = orderInfoService.lambdaQuery()
                .eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getUserId, userId)
                .one();
//        Receiver receiver = new Receiver();
//        receiver.setCompany("");
//        receiver.setName(orderInfo.getConsignee());
//        receiver.setMobile(orderInfo.getConsigneeTel());
//        receiver.setProvinceName("北京");
//        receiver.setCityName("北京市");
//        receiver.setExpAreaName("大兴区");
//        receiver.setAddress(orderInfo.getDeliveryAddress());
//        data.setReceiver(receiver);
//
//        // 商品信息
        List<OrderDetail> orderDetails = orderDetailService.lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId)
                .eq(OrderDetail::getUserId, userId)
                .list();
//        List<EOrderRequestData.Commodity> commodities = orderDetails.stream()
//                .map(orderDetail -> {
//                    EOrderRequestData.Commodity commodity = new EOrderRequestData.Commodity();
//                    commodity.setGoodsName(orderDetail.getSkuName());
//                    commodity.setGoodsquantity(orderDetail.getSkuNum());
//                    commodity.setGoodsWeight(new BigDecimal("0.5"));
//                    return commodity;
//                }).collect(Collectors.toList());
//        data.setCommodity(commodities);
//
//        // 附加服务
//        data.setAddService(null);
//
//        data.setWeight(new BigDecimal("2.0"));

//        Integer quantity = orderDetails.stream()
//                .map(OrderDetail::getSkuNum)
//                .reduce(Integer::sum)
//                .get();
//        data.setQuantity(1);
//        data.setVolume(new BigDecimal("0"));
//        data.setIsReturnPrintTemplate(1);
//        data.setRemark(orderInfo.getOrderComment());

        String RequestData = "{" +
                "'OrderCode': '" + orderId + "'," +
                "'ShipperCode': 'YTO'," +
                "'CustomerName': '客户编码'," +
                "'CustomerPwd': ''," +
                "'MonthCode': '密钥'," +
                "'SendSite': ''," +
                "'PayType': 1," +
                "'MonthCode': '1234567890'," +
                "'ExpType': 1," +
                "'Cost': 0.0," +
                "'OtherCost': 0.0," +
                "'Sender': {" +
                "'Company': '尚品汇'," +
                "'Name': '尚品汇'," +
                "'Mobile': '15018442396'," +
                "'ProvinceName': '上海'," +
                "'CityName': '上海市'," +
                "'ExpAreaName': '青浦区'," +
                "'Address': '明珠路'" +
                "}," +
                "'Receiver': {" +
                "'Company': 'GCCUI'," +
                "'Name': '" + orderInfo.getConsignee() + "'," +
                "'Mobile': '" + orderInfo.getConsigneeTel() + "'," +
                "'ProvinceName': '北京'," +
                "'CityName': '北京市'," +
                "'ExpAreaName': '大兴区'," +
                "'Address': '" + orderInfo.getDeliveryAddress() + "'" +
                "}," +
                "'Commodity': [" +
                "{" +
                "'GoodsName': '鞋子'," +
                "'Goodsquantity': 1," +
                "'GoodsWeight': 1.0" +
                "}," +
                "{" +
                "'GoodsName': '衣服'," +
                "'Goodsquantity': 1," +
                "'GoodsWeight': 1.0" +
                "}" +
                "]," +
                "'AddService': [" +
                "{" +
                "'Name': 'INSURE'," +
                "'Value': '1000'" +
                "}," +
                "]," +
                "'Weight': 1.0," +
                "'Quantity': 1," +
                "'Volume': 0.0," +
                "'IsReturnPrintTemplate':1," +
                "'Remark': '小心轻放'" +
                "}";

        // 组装应用级参数
//        ObjectMapper objectMapper = new ObjectMapper();
//        String RequestData = objectMapper.writeValueAsString(data);
        // 组装系统级参数
        Map<String, String> params = new HashMap<>();
        params.put("RequestData", urlEncoder(RequestData, "UTF-8"));
        params.put("EBusinessID", EBusinessID);
        params.put("RequestType", "1007");
        String dataSign = encrypt(RequestData, ApiKey, "UTF-8");
        params.put("DataSign", urlEncoder(dataSign, "UTF-8"));
        params.put("DataType", "2");
        // 以form表单形式提交post请求，post请求体中包含了应用级参数和系统级参数
        String result = sendPost(ReqURL, params);
        JSONObject jsonObject = JSON.parseObject(result);
        //根据公司业务处理返回的信息......
        JSONObject order = (JSONObject) jsonObject.get("Order");
        String logisticCode = order.get("LogisticCode").toString();


        // 修改订单状态
        boolean update = orderInfoService.lambdaUpdate()
                .set(OrderInfo::getOrderStatus, OrderStatus.DELEVERED.name())
                .set(OrderInfo::getProcessStatus, ProcessStatus.DELEVERED.name())
                .set(OrderInfo::getTrackingNo, logisticCode)
                .eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getUserId, userId)
                .update();
        log.info("订单: {} 已发货 物流号: {}", orderId, logisticCode);
        return jsonObject;
    }

    @Override
    public JSONObject searchLogisticStatus() {
        return null;
    }

    /**
     * MD5加密
     * str 内容
     * charset 编码方式
     *
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private String MD5(String str, String charset) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes(charset));
        byte[] result = md.digest();
        StringBuffer sb = new StringBuffer(32);
        for (int i = 0; i < result.length; i++) {
            int val = result[i] & 0xff;
            if (val <= 0xf) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString().toLowerCase();
    }

    /**
     * base64编码
     * str 内容
     * charset 编码方式
     *
     * @throws UnsupportedEncodingException
     */
    private String base64(String str, String charset) throws UnsupportedEncodingException {
        String encoded = Base64.encode(str.getBytes(charset));
        return encoded;
    }

    @SuppressWarnings("unused")
    private String urlEncoder(String str, String charset) throws UnsupportedEncodingException {
        String result = URLEncoder.encode(str, charset);
        return result;
    }

    /**
     * 电商Sign签名生成
     * content 内容
     * keyValue ApiKey
     * charset 编码方式
     *
     * @return DataSign签名
     * @throws UnsupportedEncodingException ,Exception
     */
    @SuppressWarnings("unused")
    private String encrypt(String content, String keyValue, String charset) throws Exception {
        if (keyValue != null) {
            return base64(MD5(content + keyValue, charset), charset);
        }
        return base64(MD5(content, charset), charset);
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * url 发送请求的 URL
     * params 请求的参数集合
     *
     * @return 远程资源的响应结果
     */
    @SuppressWarnings("unused")
    private String sendPost(String url, Map<String, String> params) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // POST方法
            conn.setRequestMethod("POST");
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.connect();
            // 获取URLConnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            // 发送请求参数
            if (params != null) {
                StringBuilder param = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (param.length() > 0) {
                        param.append("&");
                    }
                    param.append(entry.getKey());
                    param.append("=");
                    param.append(entry.getValue());
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                }
                System.out.println("param:" + param);
                out.write(param.toString());
            }
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }
}
