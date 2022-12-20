package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.CartInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.UserAuthUtil;
import com.atguigu.gmall.feign.product.ProductSkuDetailFeignClient;
import com.atguigu.gmall.product.entity.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductSkuDetailFeignClient skuDetailFeignClient;

    @Override
    public String determineCartKey() {
        HttpServletRequest request = UserAuthUtil.request();
        String userId = request.getHeader(RedisConst.USER_ID_HEADER);
        if (!StringUtils.isEmpty(userId)) {
            return RedisConst.CART_INFO_KEY + userId;
        }
        String tempId = request.getHeader(RedisConst.TEMP_ID_HEADER);
        return RedisConst.CART_INFO_KEY + tempId;
    }

    @Override
    public SkuInfo addToCart(Long skuId, Integer skuNum, String cartKey) {
        // 判断是否已经在购物车中
        CartInfo cartInfo = get(cartKey, skuId);
        // return value
        SkuInfo skuInfo;
        if (cartInfo == null) {
            // 不存在 添加
            cartInfo = prepareCartInfo(skuId, skuNum);
            save(cartKey, cartInfo);
            skuInfo = convertToSkuInfo(cartInfo);
        } else {
            // 存在 修改数量
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            BigDecimal currentPrice = skuDetailFeignClient.getPrice(skuId).getData();
            cartInfo.setSkuPrice(currentPrice);
            cartInfo.setUpdateTime(new Date());
            save(cartKey, cartInfo);
            skuInfo = convertToSkuInfo(cartInfo);
        }
        return skuInfo;
    }

    private SkuInfo convertToSkuInfo(CartInfo cartInfo) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(cartInfo.getSkuId());
        skuInfo.setSkuName(cartInfo.getSkuName());
        skuInfo.setSkuDefaultImg(cartInfo.getImgUrl());
        return skuInfo;
    }

    private CartInfo prepareCartInfo(Long skuId, Integer skuNum) {
        SkuInfo skuInfo = skuDetailFeignClient.getSkuInfo(skuId).getData();
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuInfo.getId());
//        cartInfo.setUserId("");
        cartInfo.setCartPrice(skuInfo.getPrice());
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setSkuNum(skuNum);
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setIsChecked(1);
        cartInfo.setCreateTime(new Date());
        cartInfo.setUpdateTime(new Date());
        return cartInfo;
    }

    @Override
    public CartInfo get(String cartKey, Long skuId) {
        Object obj = redisTemplate.opsForHash()
                .get(cartKey, skuId.toString());
        if (obj != null) {
            String json = obj.toString();
            return JSON.parseObject(json, CartInfo.class);
        }
        return null;
    }

    @Override
    public void save(String cartKey, CartInfo cartInfo) {
        redisTemplate.opsForHash()
                .put(cartKey, cartInfo.getSkuId().toString(), JSON.toJSONString(cartInfo));
    }

    @Override
    public List<CartInfo> getList(String cartKey) {
        return redisTemplate.opsForHash().values(cartKey)
                .stream()
                .map(o -> JSON.parseObject(o.toString(), CartInfo.class))
                .sorted(((o1, o2) -> o2.getUpdateTime().compareTo(o1.getCreateTime())))
                .collect(Collectors.toList());
    }
}
