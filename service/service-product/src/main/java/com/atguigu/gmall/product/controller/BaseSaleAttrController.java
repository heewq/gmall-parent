package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.BaseSaleAttr;
import com.atguigu.gmall.product.service.BaseSaleAttrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api("销售属性管理")
@RestController
@RequestMapping("/admin/product")
public class BaseSaleAttrController {
    @Autowired
    private BaseSaleAttrService baseSaleAttrService;

    @ApiOperation("获取全部销售属性")
    @GetMapping("/baseSaleAttrList")
    public Result getSaleAttrList() {
        List<BaseSaleAttr> saleAttrList = baseSaleAttrService.list();
        return Result.ok(saleAttrList);
    }
}
