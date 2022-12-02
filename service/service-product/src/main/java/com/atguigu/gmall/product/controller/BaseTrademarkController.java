package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "品牌管理")
@RestController
@RequestMapping("/admin/product")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @ApiOperation("获取所有品牌")
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList() {
        List<BaseTrademark> trademarkList = baseTrademarkService.list();
        return Result.ok(trademarkList);
    }

    /**
     * 分页获取品牌列表
     *
     * @param page
     * @param limit
     * @return
     */
    @ApiOperation("分页获取品牌列表")
    @GetMapping("/baseTrademark/{page}/{limit}")
    public Result getTrademark(@PathVariable Long page, @PathVariable Long limit) {
        Page<BaseTrademark> trademarkPage = new Page<>(page, limit);
        baseTrademarkService.page(trademarkPage);
        return Result.ok(trademarkPage);
    }

    @ApiOperation("保存品牌")
    @PostMapping("/baseTrademark/save")
    public Result save(@RequestBody BaseTrademark trademark) {
        baseTrademarkService.save(trademark);
        return Result.ok();
    }

    @ApiOperation("删除品牌")
    @DeleteMapping("/baseTrademark/remove/{id}")
    public Result remove(@PathVariable("id") Long trademarkId) {
        baseTrademarkService.removeById(trademarkId);
        return Result.ok();
    }

    @ApiOperation("修改品牌")
    @PutMapping("/baseTrademark/update")
    public Result update(@RequestBody BaseTrademark trademark) {
        baseTrademarkService.updateById(trademark);
        return Result.ok();
    }

    @ApiOperation("根据id获取品牌")
    @GetMapping("/baseTrademark/get/{id}")
    public Result get(@PathVariable("id") Long trademarkId) {
        BaseTrademark trademark = baseTrademarkService.getById(trademarkId);
        return Result.ok(trademark);
    }
}
