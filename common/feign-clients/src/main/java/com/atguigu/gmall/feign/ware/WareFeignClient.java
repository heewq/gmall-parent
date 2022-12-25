package com.atguigu.gmall.feign.ware;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "ware-manage", url = "http://localhost:9001")
public interface WareFeignClient {

    @GetMapping("/hasStock")
    String hasStock(@RequestParam("skuId") Long skuId,
                    @RequestParam("num") Integer skuNum);
}
