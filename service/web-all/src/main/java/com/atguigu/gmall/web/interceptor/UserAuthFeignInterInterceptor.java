package com.atguigu.gmall.web.interceptor;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.UserAuthUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 通过 feign 提供的拦截器机制把旧请求的 header 放入新请求的 header 中
 */
@Component
public class UserAuthFeignInterInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
//        HttpServletRequest request = CartController.map.get(Thread.currentThread());
//        HttpServletRequest request = CartController.threadLocal.get();

//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        assert requestAttributes != null;
//        HttpServletRequest request = requestAttributes.getRequest();
//        if (request == null) return;

        HttpServletRequest request = UserAuthUtil.request();

        String userId = request.getHeader(RedisConst.USER_ID_HEADER);
        if (!StringUtils.isEmpty(userId)) {
            template.header(RedisConst.USER_ID_HEADER, userId);
        }

        String tempId = request.getHeader(RedisConst.TEMP_ID_HEADER);
        if (!StringUtils.isEmpty(tempId)) {
            template.header(RedisConst.TEMP_ID_HEADER, tempId);
        }
    }
}
