package com.atguigu.gmall.common.config.thread.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.thread-pool")
public class AppThreadProperties {
    private Integer corePoolSize = 4;
    private Integer maximumPoolSize = 8;
    private Long keepAliveTime = 5L;
    private Integer workQueueSize = 1000;
}
