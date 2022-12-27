package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.CartInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.UserAuthUtil;
import com.atguigu.gmall.feign.product.ProductSkuDetailFeignClient;
import com.atguigu.gmall.product.entity.SkuInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductSkuDetailFeignClient skuDetailFeignClient;
    @Autowired
    private ThreadPoolExecutor executor;

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
        // 单个商品数量限制200
        if (cartInfo.getSkuNum() >= RedisConst.CART_ITEM_NUM_LIMIT) {
            throw new GmallException(ResultCodeEnum.CART_ITEM_NUM_OVERFLOW);
        }

        Long size = redisTemplate.opsForHash().size(cartKey);
        // 商品总数限制200
//        redisTemplate.opsForHash()
//                .put(cartKey, cartInfo.getSkuId().toString(), JSON.toJSONString(cartInfo));
//        if (size >= 200) {
//            redisTemplate.opsForHash().delete(cartKey, cartInfo.getSkuId().toString());
//            throw new GmallException(ResultCodeEnum.CART_ITEM_COUNT_OVERFLOW);
//        }

        Boolean bool = redisTemplate.opsForHash().hasKey(cartKey, cartInfo.getSkuId().toString());
        if (!bool && size + 1 >= 200) {
            throw new GmallException(ResultCodeEnum.CART_ITEM_COUNT_OVERFLOW);
        }
        redisTemplate.opsForHash()
                .put(cartKey, cartInfo.getSkuId().toString(), JSON.toJSONString(cartInfo));
    }

    @Override
    public List<CartInfo> getCartInfos(String cartKey) {
        List<CartInfo> cartInfos = redisTemplate.opsForHash().values(cartKey)
                .stream()
                .map(o -> JSON.parseObject(o.toString(), CartInfo.class))
                .sorted(((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())))
                .collect(Collectors.toList());
        // 同步最新价格
        CompletableFuture.runAsync(() -> syncPrice(cartKey, cartInfos), executor);

        return cartInfos;
    }

    private void syncPrice(String cartKey, List<CartInfo> cartInfos) {
        // todo 节流
//        Long increment = redisTemplate.opsForValue().increment("price:" + cartKey);
//        if (increment % 10 == 0) {
//        }
        cartInfos.forEach(item -> {
            BigDecimal currentPrice = skuDetailFeignClient.getPrice(item.getSkuId()).getData();
            if (Math.abs(item.getSkuPrice().doubleValue() - currentPrice.doubleValue()) >= 0.0001) {
                // 价格发生了变化
                log.info("购物车: {} 中的商品: {} 最新价格为: {}", cartKey, item.getSkuId(), currentPrice);
                item.setSkuPrice(currentPrice);
                save(cartKey, item);
            }
        });
    }

    @Override
    public void updateItemNum(String cartKey, Long skuId, Integer skuNum) {
        CartInfo cartInfo = get(cartKey, skuId);
        if (skuNum == 1 || skuNum == -1) {
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
        } else {
            cartInfo.setSkuNum(skuNum);
        }
        save(cartKey, cartInfo);
    }

    @Override
    public void check(String cartKey, Long skuId, Integer isChecked) {
        if (isChecked != 0 && isChecked != 1) {
            throw new GmallException(ResultCodeEnum.INVALID_PARAM);
        }
        CartInfo cartInfo = get(cartKey, skuId);
        cartInfo.setIsChecked(isChecked);
        save(cartKey, cartInfo);
    }

    @Override
    public void delete(String cartKey, Long skuId) {
        redisTemplate.opsForHash().delete(cartKey, skuId.toString());
    }

    @Override
    public void deleteChecked(String cartKey) {
        redisTemplate.opsForHash().delete(cartKey, getChecked(cartKey)
                .stream()
                .map(o -> o.getSkuId().toString()).toArray()
        );
    }

    @Override
    public List<CartInfo> getChecked(String cartKey) {
        return redisTemplate.opsForHash().values(cartKey)
                .stream()
                .map(o -> JSON.parseObject(o.toString(), CartInfo.class))
                .sorted(((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())))
                .filter(o -> o.getIsChecked() == 1)
                .collect(Collectors.toList());
    }

    @Override
    public List<CartInfo> display() {
        // 判断用户是否登录 且临时购物车中有数据
        HttpServletRequest request = UserAuthUtil.request();
        String tempCartKey = getCartKey(RedisConst.TEMP_ID_HEADER);
        String userCartKey = getCartKey(RedisConst.USER_ID_HEADER);

        // 用户没有登录 展示临时购物车中的数据
        if (StringUtils.isEmpty(userCartKey)) {
            // 给临时购物车设置过期时间
            Long expire = redisTemplate.getExpire(tempCartKey);
            if (expire < 0) {
                redisTemplate.expire(tempCartKey, 365, TimeUnit.DAYS);
            }
            return getCartInfos(tempCartKey);
        }

        // 用户登录 判断是否需要合并
        try {
            Long tempSize = redisTemplate.opsForHash().size(tempCartKey);
            if (tempSize > 0) {
                // 合并
                List<CartInfo> tempItems = getCartInfos(tempCartKey);
                for (CartInfo item : tempItems) {
                    addToCart(item.getSkuId(), item.getSkuNum(), userCartKey);
                }
                //删除临时购物车
                redisTemplate.delete(tempCartKey);
            }
        } catch (Exception e) {
            // 合并购物车出错 放弃合并
        }
        return getCartInfos(userCartKey);
    }

    private String getCartKey(String key) {
        HttpServletRequest request = UserAuthUtil.request();
        String header = request.getHeader(key);
        if (StringUtils.isEmpty(header)) {
            return null;
        }
        return RedisConst.CART_INFO_KEY + header;
    }
}
