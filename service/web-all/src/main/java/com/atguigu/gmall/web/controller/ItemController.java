package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.product.vo.SkuDetailVo;
import com.atguigu.gmall.web.feign.SkuDetailFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {
    @Autowired
    private SkuDetailFeignClient skuDetailFeignClient;

    /**
     * 商品详情页
     *
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String itemPage(@PathVariable Long skuId, Model model) {

        // 远程调用查询详情数据
        SkuDetailVo skuDetailVo = skuDetailFeignClient.getSkuDetails(skuId).getData();

        // 1.categoryView 分类视图 {category1Id, category2Id, category3Id, category1Name, category2Name, category3Name}
        model.addAttribute("categoryView", skuDetailVo.getCategoryView());

        // 2.skuInfo {skuName, id, skuDefaultImg, skuImageList, weight, spuId}
        model.addAttribute("skuInfo", skuDetailVo.getSkuInfo());

        // 3.price 实时价格
        model.addAttribute("price", skuDetailVo.getPrice());

        // 4.spuSaleAttrList 销售属性
        model.addAttribute("spuSaleAttrList", skuDetailVo.getSpuSaleAttrList());

        // 5.valuesSkuJson
        model.addAttribute("valuesSkuJson", skuDetailVo.getValuesSkuJson());

        // todo 6.sku规格: 平台属性

        return "item/index";
    }
}
