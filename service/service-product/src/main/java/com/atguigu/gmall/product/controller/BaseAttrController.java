package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.entity.BaseAttrInfo;
import com.atguigu.gmall.product.entity.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private BaseAttrValueService baseAttrValueService;

    @ApiOperation("根据分类id获取平台属性名和值")
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getBaseAttr(@PathVariable Long category1Id,
                              @PathVariable Long category2Id,
                              @PathVariable Long category3Id) {
        List<BaseAttrInfo> attrInfos
                = baseAttrInfoService.getBaseAttrAndValue(category1Id, category2Id, category3Id);
        return Result.ok(attrInfos);
    }

    @ApiOperation("保存/修改平台属性(属性名和属性值)")
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() == null) {
            // 保存
            baseAttrInfoService.saveAttrInfo(baseAttrInfo);
        } else {
            // 修改
            baseAttrInfoService.updateAttrInfo(baseAttrInfo);
        }
        return Result.ok();
    }

    @ApiOperation("查询某个平台属性的值")
    @GetMapping("/getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId) {
        List<BaseAttrValue> baseAttrValues = baseAttrValueService.getAttrValueList(attrId);
        return Result.ok(baseAttrValues);
    }
}
