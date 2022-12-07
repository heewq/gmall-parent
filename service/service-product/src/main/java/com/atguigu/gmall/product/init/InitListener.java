package com.atguigu.gmall.product.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class InitListener implements SpringApplicationRunListener {
    public InitListener(SpringApplication application, String[] args) {
    }

    @Override
    public void starting() {
        log.info("====STARTING====");
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        log.info("====STARTED====");
    }
}
