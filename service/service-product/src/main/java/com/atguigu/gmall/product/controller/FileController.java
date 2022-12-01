package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.FileService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api("文件管理")
@RestController
@RequestMapping("/admin/product")
public class FileController {
    @Autowired
    private FileService fileService;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/fileUpload")
    public Result upload(@RequestPart("file") MultipartFile file) {
        log.info("file: {}", file.getSize());

        String path = fileService.upload(file);
        return Result.ok(path);
    }
}
