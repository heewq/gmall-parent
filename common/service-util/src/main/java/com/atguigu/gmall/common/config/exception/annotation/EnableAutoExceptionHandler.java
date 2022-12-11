package com.atguigu.gmall.common.config.exception.annotation;

import com.atguigu.gmall.common.config.exception.GlobalExceptionAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(GlobalExceptionAutoConfiguration.class)
public @interface EnableAutoExceptionHandler {
}
