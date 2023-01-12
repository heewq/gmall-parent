package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.gateway.properties.AuthUrlProperties;
import com.atguigu.gmall.user.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class UserAuthGatewayFilter implements GlobalFilter {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AuthUrlProperties authUrlProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 静态资源直接放行
        long count = authUrlProperties.getAnyoneUrl()
                .stream()
                .filter(pattern -> pathMatcher.match(pattern, path))
                .count();
        if (count > 0) {
            return chain.filter(exchange);
        }

        // 内部接口拒绝访问
        long denyCount = authUrlProperties.getDenyUrl()
                .stream()
                .filter(pattern -> pathMatcher.match(pattern, path))
                .count();
        if (denyCount > 0) {
            Result<String> result = Result.build("", ResultCodeEnum.PERMISSION);
            return responseJson(exchange, result);
        }

        // 有限权限访问[需要登录]
        long authCount = authUrlProperties.getAuthUrl()
                .stream()
                .filter(pattern -> pathMatcher.match(pattern, path))
                .count();
        if (authCount > 0) {
            String token = getAuthInfo(exchange, "token");
            UserInfo userInfo = getUserInfo(token);
            if (StringUtils.isEmpty(token) || userInfo == null) {
                // redirect
                return redirectTo(exchange, authUrlProperties.getLoginPage());
            }
        }
        // 正常请求
        // 透传id
        return userIdThrough(chain, exchange);
    }

    private Mono<Void> userIdThrough(GatewayFilterChain chain, ServerWebExchange exchange) {
        ServerHttpRequest.Builder reqBuilder = exchange.getRequest().mutate();

        // 用户id
        UserInfo userInfo = getUserInfo(getAuthInfo(exchange, "token"));
        if (userInfo != null) {
            reqBuilder.header(RedisConst.USER_ID_HEADER, userInfo.getId().toString()).build();
        }

        // 临时id
        String tempId = getAuthInfo(exchange, "userTempId");
        if (!StringUtils.isEmpty(tempId)) {
            reqBuilder.header(RedisConst.TEMP_ID_HEADER, tempId)
                    .build();
        }

        // 放行
        return chain.filter(exchange);
    }

    private Mono<Void> redirectTo(ServerWebExchange exchange, String loginPage) {
        ServerHttpResponse response = exchange.getResponse();
        // originUrl
        URI uri = exchange.getRequest().getURI();
        loginPage += "?originUrl=" + uri;

        // 重定向:
        //  code: 302
        //  响应头 Location: url
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().set(HttpHeaders.LOCATION, loginPage);

        return response.setComplete();
    }

    private Mono<Void> responseJson(ServerWebExchange exchange, Result<String> result) {
        ServerHttpResponse response = exchange.getResponse();
        // 响应数据
        String json = JSON.toJSONString(result);
        DataBuffer dataBuffer = response.bufferFactory()
                .wrap(json.getBytes(StandardCharsets.UTF_8));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(dataBuffer));
    }

    private UserInfo getUserInfo(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        String jsonString = redisTemplate.opsForValue().get("login:user:" + token);
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        return JSON.parseObject(jsonString, UserInfo.class);
    }

    /**
     * 从请求头或cookie中获取token
     *
     * @param exchange
     * @return
     */
    private String getAuthInfo(ServerWebExchange exchange, String authKey) {
        ServerHttpRequest request = exchange.getRequest();
        String authInfo;
        authInfo = request.getHeaders().getFirst(authKey);
        if (!StringUtils.isEmpty(authInfo)) {
            return authInfo;
        }
        HttpCookie cookie = request.getCookies().getFirst(authKey);
        if (cookie != null) {
            authInfo = cookie.getValue();
        }
        return authInfo;
    }
}
