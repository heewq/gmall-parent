package com.atguigu.gmall.search.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.search.Goods;
import com.atguigu.gmall.search.search.SearchService;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inner/rpc/search")
public class SearchRpcController {
    @Autowired
    private SearchService searchService;

    /**
     * 更新热度分 hotScore
     *
     * @param skuId
     * @param score
     * @return
     */
    @GetMapping("/hotScore/{skuId}/{score}")
    public Result updateHotScore(@PathVariable Long skuId, @PathVariable Long score) {
        searchService.updateHotScore(skuId, score);
        return Result.ok();
    }

    /**
     * 上架
     *
     * @param goods
     * @return
     */
    @PostMapping("/onSale/goods")
    public Result onSale(@RequestBody Goods goods) {
        searchService.onSale(goods);
        return Result.ok();
    }

    /**
     * 下架
     *
     * @param skuId
     * @return
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId) {
        searchService.cancelSale(skuId);
        return Result.ok();
    }

    /**
     * 检索商品
     *
     * @param paramVo
     * @return
     */
    @PostMapping("/searchGoods")
    public Result<SearchRespVo> search(@RequestBody SearchParamVo paramVo) {
        // 检索
        SearchRespVo respVo = searchService.search(paramVo);
        return Result.ok(respVo);
    }
}
