package com.atguigu.gmall.common.config.thread;

import com.atguigu.gmall.common.config.thread.properties.AppThreadProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(AppThreadProperties.class)
public class AppThreadPoolAutoConfiguration {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(AppThreadProperties appThreadProperties) {
        return new ThreadPoolExecutor(
                appThreadProperties.getCorePoolSize(),
                appThreadProperties.getMaximumPoolSize(),
                appThreadProperties.getKeepAliveTime(),
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(appThreadProperties.getWorkQueueSize()),
//                Executors.defaultThreadFactory(),
                Thread::new,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
