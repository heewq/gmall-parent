package com.atguigu.gmall.common.config.exception;

import com.atguigu.gmall.common.config.exception.handler.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionAutoConfiguration {
}
