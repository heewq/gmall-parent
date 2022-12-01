package com.atguigu.gmall.product;

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;

@SpringBootTest
public class MinioTest {
    @Test
    public void uploadTest() throws Exception {
        // 创建 MinioClient
        MinioClient minioClient = new MinioClient("http://192.168.206.100:9000", "admin", "admin123456");

        // 检查存储桶是否已经存在
        boolean isExist = minioClient.bucketExists("mall-oss");
        if (isExist) {
            System.out.println("Bucket already exists.");
        } else {
            // 创建一个名为mall-oss的存储桶
            minioClient.makeBucket("mall-oss");
        }

        String path = "E:\\Data\\atguigu\\尚品汇\\资料\\03 商品图片\\4.png";
        FileInputStream stream = new FileInputStream(path);

        PutObjectOptions options = new PutObjectOptions(stream.available(), -1);
        options.setContentType("image/png");
        // 使用putObject上传一个文件到存储桶中
        minioClient.putObject("mall-oss", "7.png", stream, options);
        System.out.println("File is successfully uploaded to `mall-oss` bucket.");
    }
}
