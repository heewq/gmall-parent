package com.atguigu.gmall.common.config.minio;

import com.atguigu.gmall.common.config.minio.config.MinioConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({MinioConfiguration.class})
@Configuration
public class MinioAutoConfiguration {
}
