package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.feign.SkuDetailFeignClient;
import com.atguigu.gmall.item.service.CacheService;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.product.entity.SkuImage;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import com.atguigu.gmall.product.vo.SkuDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.atguigu.gmall.product.vo.SkuDetailVo.CategoryViewDTO;

@Slf4j
@Service
public class SkuDetailServiceImpl implements SkuDetailService {
    @Autowired
    private SkuDetailFeignClient skuDetailFeignClient;
    @Autowired
    private ThreadPoolExecutor executor;

    private final Map<Long, SkuDetailVo> cache = new ConcurrentHashMap<>();
    ReentrantLock lock = new ReentrantLock();
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CacheService cacheService;

    private static CategoryViewDTO getCategoryViewDTO(CategoryTreeVo categoryTree) {
        CategoryViewDTO categoryViewDTO = new CategoryViewDTO();
        categoryViewDTO.setCategory1Id(categoryTree.getCategoryId());
        categoryViewDTO.setCategory1Name(categoryTree.getCategoryName());
        categoryViewDTO.setCategory2Id(categoryTree.getCategoryChild().get(0).getCategoryId());
        categoryViewDTO.setCategory2Name(categoryTree.getCategoryChild().get(0).getCategoryName());
        categoryViewDTO.setCategory3Id(categoryTree.getCategoryChild().get(0).getCategoryChild().get(0).getCategoryId());
        categoryViewDTO.setCategory3Name(categoryTree.getCategoryChild().get(0).getCategoryChild().get(0).getCategoryName());
        return categoryViewDTO;
    }

    @Override
    public SkuDetailVo getSkuDetailData(Long skuId) {
        SkuDetailVo returnVal;

        // 1.查询缓存
        log.info("商品详情查询开始...");
        returnVal = cacheService.getFromCache(skuId);
        if (returnVal == null) {
            // 缓存未命中
            // 位图判断数据库中是否存在
            Boolean contain = cacheService.mightContain(skuId);
            if (!contain) {
                log.info("bitmap中不存在 疑似攻击请求");
                return null;
            }
            // 回源
            log.info("bitmap中存在 准备回源... 需要加锁防止击穿");

            // 拦截缓存击穿风险
            if (lock.tryLock()) {
                // 抢锁成功
                log.info("加锁成功 正在回源...");
                returnVal = getData(skuId);
                // 保存到缓存
                // 如果这里使用的是BloomFilter,可能会发生误判,所以需要缓存null值
                cacheService.saveData(skuId, returnVal);
                lock.unlock();
            } else {
                // 抢锁失败 500ms后直接查缓存
                log.info("加锁失败 稍后直接查询缓存并返回数据...");
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                    returnVal = cacheService.getFromCache(skuId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return returnVal;
    }

    // 缓存空值
    public SkuDetailVo getSkuDetailDataNullSave(Long skuId) {
        // sku:info:52 == json
        String json = redisTemplate.opsForValue().get("sku:info:" + skuId);

        if (StringUtils.isEmpty(json)) {
            SkuDetailVo skuDetailVo = getData(skuId);

            String jsonData = "x";
            if (skuDetailVo.getSkuInfo() != null) {
                jsonData = JSON.toJSONString(skuDetailVo);
            } else {
                skuDetailVo = null;
            }
            redisTemplate.opsForValue().set("sku:info:" + skuId, jsonData, 7, TimeUnit.DAYS);
            return skuDetailVo;
        }
        if ("x".equals(json)) {
            log.info("疑似攻击请求");
            return null;
        }
        return JSON.parseObject(json, SkuDetailVo.class);
    }

    /**
     * 本地缓存
     *
     * @param skuId
     * @return
     */
    public SkuDetailVo getSkuDetailDataLocalCache(Long skuId) {
        // 先查缓存
        SkuDetailVo data = cache.get(skuId);

        if (data == null) {
            log.info("// 缓存未命中");
            // 回源
            data = getData(skuId);

            // 同步到缓存
            cache.put(skuId, data);
        }

        return data;
    }

    /**
     * 查询商品详情
     *
     * @param skuId
     * @return
     */
    private SkuDetailVo getData(Long skuId) {
        SkuDetailVo data = new SkuDetailVo();

        CompletableFuture<SkuInfo> skuInfoCompletableFuture
                = CompletableFuture.supplyAsync(() -> skuDetailFeignClient.getSkuInfo(skuId).getData(), executor);

        skuInfoCompletableFuture = skuInfoCompletableFuture
                .thenApplyAsync(res -> {
                    if (res == null) return null;
                    List<SkuImage> images = skuDetailFeignClient.getImages(skuId).getData();
                    res.setSkuImageList(images);
                    data.setSkuInfo(res);
                    return res;
                }, executor);

        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture
                .thenAcceptAsync(res -> {
                    if (res == null) return;
                    CategoryTreeVo categoryTree = skuDetailFeignClient.getCategoryTreeWithC3Id(res.getCategory3Id()).getData();
                    CategoryViewDTO categoryViewDTO = getCategoryViewDTO(categoryTree);
                    data.setCategoryView(categoryViewDTO);
                }, executor);


        CompletableFuture<Void> priceCompletableFuture = CompletableFuture
                .runAsync(() -> {
                    try {
                        BigDecimal price = skuDetailFeignClient.getPrice(skuId).getData();
                        data.setPrice(price);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executor);

        CompletableFuture<Void> saleAttrCompletableFuture = skuInfoCompletableFuture
                .thenAcceptAsync(res -> {
                    if (res == null) return;
                    List<SpuSaleAttr> spuSaleAttrs = skuDetailFeignClient.getSpuSaleAttrs(res.getSpuId(), skuId).getData();
                    data.setSpuSaleAttrList(spuSaleAttrs);
                }, executor);

        CompletableFuture<Void> valueSkuJsonCompletableFuture = skuInfoCompletableFuture
                .thenAcceptAsync(res -> {
                    if (res == null) return;
                    String valuesSkuJson = skuDetailFeignClient.getValuesSkuJson(res.getSpuId()).getData();
                    data.setValuesSkuJson(valuesSkuJson);
                }, executor);

        CompletableFuture.allOf(
                        categoryViewCompletableFuture,
                        priceCompletableFuture,
                        saleAttrCompletableFuture,
                        valueSkuJsonCompletableFuture)
                .join();
        return data;
    }
}