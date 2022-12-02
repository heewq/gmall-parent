package com.atguigu.gmall.common.config.minio.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {
    String endpoint;
    String accessKey;
    String secretKey;
    String bucketName;
}
