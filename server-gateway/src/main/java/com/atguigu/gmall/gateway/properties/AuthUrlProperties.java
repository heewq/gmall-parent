package com.atguigu.gmall.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthUrlProperties {
    // 直接放行
    private List<String> anyoneUrl;
    // 拒绝访问
    private List<String> denyUrl;
    // 需要认证
    private List<String> authUrl;
    // 登录页面
    private String loginPage;
}
