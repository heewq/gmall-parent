package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.BaseAttrInfo;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台属性
 */
@Api(tags = "平台属性")
@RequestMapping("/admin/product")
@RestController
public class BaseAttrController {
    @Autowired
    private BaseAttrInfoService baseAttrInfoService;

    @ApiOperation("根据分类id获取平台属性名和值")
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getBaseAttr(@PathVariable Long category1Id,
                              @PathVariable Long category2Id,
                              @PathVariable Long category3Id) {
        List<BaseAttrInfo> attrInfos
                = baseAttrInfoService.getBaseAttrAndValue(category1Id, category2Id, category3Id);
        return Result.ok(attrInfos);
    }
}
