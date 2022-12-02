package com.atguigu.gmall.common.config.minio;

import com.atguigu.gmall.common.config.minio.config.MinioConfiguration;
import com.atguigu.gmall.common.config.minio.properties.MinioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableConfigurationProperties(MinioProperties.class)
@Import({MinioConfiguration.class})
@Configuration
public class MinioAutoConfiguration {
}
