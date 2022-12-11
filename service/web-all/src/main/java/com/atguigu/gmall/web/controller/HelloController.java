package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/div/{num}")
    public Result div(@PathVariable Integer num) {
        return Result.ok(10 / num);
    }
}
