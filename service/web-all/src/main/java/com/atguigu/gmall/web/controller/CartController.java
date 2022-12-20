package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.product.entity.SkuInfo;
import feign.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {
    @Autowired
    private CartFeignClient cartFeignClient;

//    public static Map<Thread, HttpServletRequest> map = new ConcurrentHashMap<>();
//    public static ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();

    /**
     * 把商品添加到购物车
     *
     * @param skuId
     * @param skuNum
     * @param model
     * @return
     */
    @GetMapping("/addCart.html")
    public String addCart(
            @RequestParam Long skuId,
            @RequestParam Integer skuNum,
            Model model) {

//        map.put(Thread.currentThread(), request);
//        threadLocal.set(request);

        Request.Options options = new Request.Options(5000, 5000);
        Result<SkuInfo> result = cartFeignClient.addCart(skuId, skuNum, options);

//        map.remove(Thread.currentThread()); // 防止OOM
//        threadLocal.remove();

        model.addAttribute("skuInfo", result.getData());
        model.addAttribute("skuNum", skuNum);
        return "cart/addCart";
    }

    @GetMapping("/cart.html")
    public String cart() {
        return "cart/index";
    }

    @GetMapping("/cart/deleteChecked")
    public String deleteChecked() {
        cartFeignClient.deleteChecked();
        return "redirect:http://cart.gmall.com/cart.html";
    }
}
