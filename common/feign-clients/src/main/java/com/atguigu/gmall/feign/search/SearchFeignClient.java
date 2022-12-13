package com.atguigu.gmall.feign.search;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.search.Goods;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchRespVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-search")
@RequestMapping("/api/inner/rpc/search")
public interface SearchFeignClient {
    /**
     * 检索商品
     *
     * @param paramVo
     * @return
     */
    @PostMapping("/searchGoods")
    Result<SearchRespVo> search(@RequestBody SearchParamVo paramVo);

    /**
     * 上架
     *
     * @param goods
     * @return
     */
    @PostMapping("/onSale/goods")
    Result onSale(@RequestBody Goods goods);

    /**
     * 下架
     *
     * @param skuId
     * @return
     */
    @GetMapping("/cancelSale/{skuId}")
    Result cancelSale(@PathVariable Long skuId);

    @GetMapping("/hotScore/{skuId}/{score}")
    Result updateHotScore(@PathVariable Long skuId, @PathVariable Long score);
}
