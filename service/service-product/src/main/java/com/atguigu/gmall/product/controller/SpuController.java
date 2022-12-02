package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.SpuInfo;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.product.vo.SpuSaveInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api("SPU管理")
@RestController
@RequestMapping("/admin/product")
public class SpuController {
    @Autowired
    private SpuInfoService spuInfoService;

    @ApiOperation("根据三级分类id获取SPU列表")
    @GetMapping("/{page}/{limit}")
    public Result getSpuInfo(@PathVariable Long page,
                             @PathVariable Long limit,
                             @RequestParam("category3Id") Long category3Id) {
        Page<SpuInfo> spuInfoPage
                = spuInfoService.lambdaQuery()
                .eq(SpuInfo::getCategory3Id, category3Id)
                .page(new Page<>(page, limit));
        return Result.ok(spuInfoPage);
    }

    @ApiOperation("保存SPU")
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuSaveInfoVo spuSaveInfoVo) {
        spuInfoService.saveSpuInfo(spuSaveInfoVo);
        return Result.ok();
    }
}
