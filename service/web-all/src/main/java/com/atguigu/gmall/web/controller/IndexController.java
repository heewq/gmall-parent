package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.feign.product.CategoryFeignClient;
import com.atguigu.gmall.product.vo.CategoryTreeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private CategoryFeignClient categoryFeignClient;

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        List<CategoryTreeVo> list = categoryFeignClient.getCategoryTree().getData();
        model.addAttribute("list", list);
        return "index/index";
    }
}
