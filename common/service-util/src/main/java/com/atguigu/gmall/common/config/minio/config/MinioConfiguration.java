package com.atguigu.gmall.common.config.minio.config;

import com.atguigu.gmall.common.config.minio.properties.MinioProperties;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfiguration {
    //    @Autowired
    //    private MinioProperties minioProperties;
    @Bean // 给容器中放入组件 参数从容器中获取
    public MinioClient minioClient(MinioProperties minioProperties) throws Exception {
        MinioClient minioClient = new MinioClient(minioProperties.getEndpoint(),
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey());

        // 检查存储桶是否已经存在
        boolean isExist = minioClient.bucketExists(minioProperties.getBucketName());
        if (!isExist) {
            minioClient.makeBucket(minioProperties.getBucketName());
        }
        return minioClient;
    }
}
