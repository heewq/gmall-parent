package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.BaseCategory1;
import com.atguigu.gmall.product.entity.BaseCategory2;
import com.atguigu.gmall.product.entity.BaseCategory3;
import com.atguigu.gmall.product.service.BaseCategory2Service;
import com.atguigu.gmall.product.service.BaseCategory3Service;
import com.atguigu.gmall.product.service.BaseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "分类管理")
@RestController
@RequestMapping("/admin/product")
public class BaseCategoryController {
    @Autowired
    private BaseCategoryService baseCategoryService;
    @Autowired
    private BaseCategory2Service baseCategory2Service;
    @Autowired
    private BaseCategory3Service baseCategory3Service;

    /**
     * 查询所有一级分类
     *
     * @return
     */
    @ApiOperation("查询所有一级分类")
    @GetMapping("/getCategory1")
    public Result getCategory1() {
        List<BaseCategory1> category1s = baseCategoryService.list();
        return Result.ok(category1s);
    }

    /**
     * 查询某个一级分类下的二级分类
     *
     * @param category1Id
     * @return
     */
    @ApiOperation("查询所有二级分类")
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id) {
        List<BaseCategory2> baseCategory2s = baseCategory2Service.getCategory2ByC1Id(category1Id);
        return Result.ok(baseCategory2s);
    }

    /**
     * 查询某个二级分类下的三级分类
     *
     * @param category2Id
     * @return
     */
    @ApiOperation("查询所有三级分类")
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id) {
        List<BaseCategory3> category3s = baseCategory3Service.getCategory3ByC2Id(category2Id);
        return Result.ok(category3s);
    }
}
