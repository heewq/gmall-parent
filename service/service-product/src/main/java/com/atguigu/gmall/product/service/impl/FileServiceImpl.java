package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.config.minio.properties.MinioProperties;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.product.service.FileService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private MinioProperties minioProperties;
    @Autowired
    private MinioClient minioClient;

    @Override
    public String upload(MultipartFile file) {
        try {
//            // 创建 MinioClient
//            MinioClient minioClient = new MinioClient(
//                    minioProperties.getEndpoint(),
//                    minioProperties.getAccessKey(),
//                    minioProperties.getSecretKey());
//
//            // 检查存储桶是否已经存在
//            boolean isExist = minioClient.bucketExists(minioProperties.getBucketName());
//            if (!isExist) {
//                minioClient.makeBucket(minioProperties.getBucketName());
//            }

            String date = DateUtil.formatDate(new Date());
            String filename = date + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            InputStream stream = file.getInputStream();
            PutObjectOptions options = new PutObjectOptions(file.getSize(), -1);
            options.setContentType(file.getContentType());
            // 上传
            minioClient.putObject(minioProperties.getBucketName(), filename, stream, options);
            return minioProperties.getEndpoint() + "/" + minioProperties.getBucketName() + "/" + filename;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
