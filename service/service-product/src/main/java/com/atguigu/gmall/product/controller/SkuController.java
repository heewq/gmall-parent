package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.vo.SkuSaveVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api("SKU管理")
@RestController
@RequestMapping("/admin/product")
public class SkuController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 上架
     *
     * @param skuId
     * @return
     */
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId) {
        skuInfoService.onSale(skuId);
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
        skuInfoService.cancelSale(skuId);
        return Result.ok();
    }

    @ApiOperation("查询SKU分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Result getSkuList(@PathVariable Long page, @PathVariable Long limit) {
        Page<SkuInfo> skuPage = skuInfoService.lambdaQuery().page(new Page<>(page, limit));
        return Result.ok(skuPage);
    }

    @ApiOperation("保存SKU")
    @PostMapping("/saveSkuInfo")
    public Result saveSku(@RequestBody SkuSaveVo skuSaveVo) {
        skuInfoService.saveSkuInfo(skuSaveVo);
        return Result.ok();
    }
}
