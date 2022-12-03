package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.SpuImage;
import com.atguigu.gmall.product.entity.SpuInfo;
import com.atguigu.gmall.product.entity.SpuSaleAttr;
import com.atguigu.gmall.product.service.SpuImageService;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.vo.SpuSaveInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("SPU管理")
@RestController
@RequestMapping("/admin/product")
public class SpuController {
    @Autowired
    private SpuInfoService spuInfoService;
    @Autowired
    private SpuImageService spuImageService;
    @Autowired
    private SpuSaleAttrService spuSaleAttrService;

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

    @ApiOperation("查询SPU图片")
    @GetMapping("/spuImageList/{id}")
    public Result spuImageList(@PathVariable("id") Long spuId) {
        List<SpuImage> spuImageList = spuImageService.lambdaQuery().eq(SpuImage::getSpuId, spuId).list();
        return Result.ok(spuImageList);
    }

    ///spuSaleAttrList/28
    @ApiOperation("查询SPU销售属性和属性值")
    @GetMapping("/spuSaleAttrList/{id}")
    public Result spuSaleAttrList(@PathVariable("id") Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList
                = spuSaleAttrService.getSpuSaleAttrs(spuId);
        return Result.ok(spuSaleAttrList);
    }
}
