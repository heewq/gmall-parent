package com.atguigu.gmall.common.util;

import com.atguigu.gmall.common.constant.RedisConst;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class UserAuthUtil {

    /**
     * 获取旧请求
     *
     * @return
     */
    public static HttpServletRequest request() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        return requestAttributes.getRequest();
    }

    public static Long getUserId() {
        String userId = request().getHeader(RedisConst.USER_ID_HEADER);
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        return Long.parseLong(userId);
    }
}
