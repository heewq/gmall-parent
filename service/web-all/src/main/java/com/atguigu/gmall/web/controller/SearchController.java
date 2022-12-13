package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.feign.search.SearchFeignClient;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    private SearchFeignClient searchFeignClient;


    @GetMapping("/list.html")
    public String search(SearchParamVo param, Model model) {

        // 远程调用检索服务
        SearchRespVo resp = searchFeignClient.search(param).getData();

        // 检索参数 SearchParamVo
        model.addAttribute("searchParam", resp.getSearchParam());

        // 品牌breadcrumb string
        model.addAttribute("trademarkParam", resp.getTrademarkParam());

        // 平台属性breadcrumb [{attrName, attrValue, attrId}]
        model.addAttribute("propsParamList", resp.getPropsParamList());

        // 品牌列表 [{tmId, tmName, tmLogoUrl}]
        model.addAttribute("trademarkList", resp.getTrademarkList());

        // 属性列表 [{attrId, attrName, attrValueList:[string]}]
        model.addAttribute("attrsList", resp.getAttrsList());

        // url参数
        model.addAttribute("urlParam", resp.getUrlParam());

        // 排序信息 {type, sort}
        model.addAttribute("orderMap", resp.getOrderMap());

        // 商品列表 [{商品信息}]
        model.addAttribute("goodsList", resp.getGoodsList());

        // 页码
        model.addAttribute("pageNo", resp.getPageNo());

        // 总页码
        model.addAttribute("totalPages", resp.getTotalPages());

        return "list/index"; //检索结果页
    }
}
