package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.feign.product.ProductSkuDetailFeignClient;
import com.atguigu.gmall.feign.search.SearchFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.product.entity.SkuImage;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import com.atguigu.gmall.product.vo.SkuDetailVo;
import com.atguigu.gmall.starter.cache.aspect.annotation.MallCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.atguigu.gmall.product.vo.SkuDetailVo.CategoryViewDTO;

@Slf4j
@Service
public class SkuDetailServiceImpl implements SkuDetailService {
    @Autowired
    private ProductSkuDetailFeignClient productSkuDetailFeignClient;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SearchFeignClient searchFeignClient;

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

/*
    public SkuDetailVo getSkuDetailDataWithDistributeLock(Long skuId) {
//        log.info("????????????...");
        SkuDetailVo skuDetail = cacheService.getFromCache(skuId);
        if (skuDetail != null) {
            return skuDetail;
        }
//        log.info("???????????????...??????bitmap????????????????????????");
        Boolean contain = cacheService.mightContain(skuId);
        if (!contain) {
//            log.info("bitmap???????????? ??????????????????");
            return null;
        }
//        log.info("??????...????????????");

        RLock lock = redissonClient.getLock(RedisConst.SKU_LOCK + skuId);
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
//                log.info("???????????? ????????????");
                skuDetail = getData(skuId);

//                log.info("????????????????????????...");
                cacheService.saveData(skuId, skuDetail);

                return skuDetail;
            } else {
//                log.info("???????????? ??????300ms???????????????");
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return cacheService.getFromCache(skuId);
            }
        } finally {
            if (locked) {
                try {
//                    log.info("????????????...");
                    lock.unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
*/

    /*
     * ?????????
     */
/*
    public SkuDetailVo getSkuDetailDataWithLocalLock(Long skuId) {
        SkuDetailVo returnVal;

        // 1.????????????
        log.info("????????????????????????...");
        returnVal = cacheService.getFromCache(skuId);
        if (returnVal == null) {
            // ???????????????
            // ????????????????????????????????????
            Boolean contain = cacheService.mightContain(skuId);
            if (!contain) {
                log.info("bitmap???????????? ??????????????????");
                return null;
            }
            // ??????
            log.info("bitmap????????? ????????????... ????????????????????????");

            // ????????????????????????
            if (lock.tryLock()) {
                // ????????????
                log.info("???????????? ????????????...");
                returnVal = getData(skuId);
                // ???????????????
                // ????????????????????????BloomFilter,?????????????????????,??????????????????null???
                cacheService.saveData(skuId, returnVal);
                lock.unlock();
            } else {
                // ???????????? 500ms??????????????????
                log.info("???????????? ???????????????????????????????????????...");
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
*/

    /*
     * ????????????
     */
/*
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
            log.info("??????????????????");
            return null;
        }
        return JSON.parseObject(json, SkuDetailVo.class);
    }
*/

    /*
     * ????????????
     */
/*    public SkuDetailVo getSkuDetailDataLocalCache(Long skuId) {
        // ????????????
        SkuDetailVo data = cache.get(skuId);

        if (data == null) {
            log.info("// ???????????????");
            // ??????
            data = getData(skuId);

            // ???????????????
            cache.put(skuId, data);
        }

        return data;
    }*/

    @Override
    @MallCache(
            cacheKey = RedisConst.SKU_DETAIL_CACHE + "#{#args[0]}",
            bitmapName = RedisConst.SKUID_BITMAP,
            bitmapKey = "#{#args[0]}",
            lockKey = RedisConst.SKU_LOCK + "#{#args[0]}",
            ttl = 7,
            unit = TimeUnit.DAYS
    )
    public SkuDetailVo getSkuDetailData(Long skuId) {
        return getData(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        CompletableFuture.runAsync(() -> {
            Long increment = redisTemplate.opsForValue().increment(RedisConst.SKU_HOTSCORE + skuId);
            if (increment % 100 == 0) {
                // ????????????ES????????????
                searchFeignClient.updateHotScore(skuId, increment);
            }
        }, executor);
    }

    /**
     * ??????????????????
     *
     * @param skuId
     * @return
     */
    private SkuDetailVo getData(Long skuId) {
        SkuDetailVo data = new SkuDetailVo();

        CompletableFuture<SkuInfo> skuInfoCompletableFuture
                = CompletableFuture.supplyAsync(() -> productSkuDetailFeignClient.getSkuInfo(skuId).getData(), executor);

        skuInfoCompletableFuture = skuInfoCompletableFuture
                .thenApplyAsync(res -> {
                    if (res == null) return null;
                    List<SkuImage> images = productSkuDetailFeignClient.getImages(skuId).getData();
                    res.setSkuImageList(images);
                    data.setSkuInfo(res);
                    return res;
                }, executor);

        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture
                .thenAcceptAsync(res -> {
                    if (res == null) return;
                    CategoryTreeVo categoryTree = productSkuDetailFeignClient.getCategoryTreeWithC3Id(res.getCategory3Id()).getData();
                    CategoryViewDTO categoryViewDTO = getCategoryViewDTO(categoryTree);
                    data.setCategoryView(categoryViewDTO);
                }, executor);


        CompletableFuture<Void> priceCompletableFuture = CompletableFuture
                .runAsync(() -> {
                    try {
                        BigDecimal price = productSkuDetailFeignClient.getPrice(skuId).getData();
                        data.setPrice(price);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executor);

        CompletableFuture<Void> saleAttrCompletableFuture = skuInfoCompletableFuture
                .thenAcceptAsync(res -> {
                    if (res == null) return;
                    List<SpuSaleAttr> spuSaleAttrs = productSkuDetailFeignClient.getSpuSaleAttrs(res.getSpuId(), skuId).getData();
                    data.setSpuSaleAttrList(spuSaleAttrs);
                }, executor);

        CompletableFuture<Void> valueSkuJsonCompletableFuture = skuInfoCompletableFuture
                .thenAcceptAsync(res -> {
                    if (res == null) return;
                    String valuesSkuJson = productSkuDetailFeignClient.getValuesSkuJson(res.getSpuId()).getData();
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
