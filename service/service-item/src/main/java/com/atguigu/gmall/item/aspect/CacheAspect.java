package com.atguigu.gmall.item.aspect;

import com.atguigu.gmall.item.aspect.annotation.MallCache;
import com.atguigu.gmall.item.service.CacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class CacheAspect {
    // 创建表达式解析器
    SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取指定注解
     *
     * @param joinPoint
     * @param clz
     * @param <T>
     * @return
     */
    private static <T extends Annotation> T getAnnotation(ProceedingJoinPoint joinPoint, Class<T> clz) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaredAnnotation(clz);
    }

    /**
     * 获取目标方法的返回值类型
     *
     * @param joinPoint
     * @return
     */
    private static Type getReturnType(ProceedingJoinPoint joinPoint) {
        // 方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getGenericReturnType();
    }

    @Pointcut(value = "@annotation(com.atguigu.gmall.item.aspect.annotation.MallCache)")
    public void pc() {
    }

    @Around(value = "pc()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RLock lock = null;
        boolean tryLock = false;
        try {
            Type returnType = getReturnType(joinPoint);

            MallCache mallCache = getAnnotation(joinPoint, MallCache.class);
            String cacheKey = mallCache.cacheKey();
            if (StringUtils.isEmpty(cacheKey)) {
                cacheKey = getDefaultCacheKey(joinPoint);
            } else {
                cacheKey = evalExpression(mallCache.cacheKey(), joinPoint, String.class);
            }
            Object fromCache = cacheService.getCacheDate(cacheKey, returnType);
            if (fromCache != null) {
                return fromCache;
            }

            String bitmap = mallCache.bitmapName();
            if (!StringUtils.isEmpty(bitmap)) { // 如果没有指定bitmap则不使用bitmap
                Long bitmapKey = evalExpression(mallCache.bitmapKey(), joinPoint, Long.class);
                // 断言
                Assert.notNull(bitmapKey, "必须传递 bitmap 索引");
                Boolean contain = cacheService.mightContain(bitmap, bitmapKey);
                if (!contain) {
                    return null;
                }
            }
            String lockKey = mallCache.lockKey();
            if (StringUtils.isEmpty(lockKey)) {
                lockKey += "lock:" + cacheKey;
            } else {
                lockKey = evalExpression(lockKey, joinPoint, String.class);
            }
            lock = redissonClient.getLock(lockKey);
            tryLock = lock.tryLock();
            if (tryLock) {
                // 缓存双检查
                fromCache = cacheService.getCacheDate(cacheKey, returnType);
                if (fromCache != null) {
                    return fromCache;
                }

                Object proceed = joinPoint.proceed();

                cacheService.saveCache(cacheKey, proceed, mallCache.ttl(), mallCache.unit());
                return proceed;
            }
            TimeUnit.MILLISECONDS.sleep(300);
            return cacheService.getCacheDate(cacheKey, returnType);
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }
    }

    private String getDefaultCacheKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String declaringTypeName = signature.getDeclaringType().toString().replace("class ", "");
        Object params = Arrays.stream(joinPoint.getArgs()).reduce((o1, o2) -> o1 + "_" + o2.toString()).get();
        // 默认缓存key
        return declaringTypeName + ":" + methodName + ":" + params;
    }

    /**
     * 计算指定表达式的值
     *
     * @param expr
     * @param joinPoint
     * @return
     */
    private <T> T evalExpression(String expr, ProceedingJoinPoint joinPoint, Class<T> returnType) {
        // 解析表达式
        Expression expression = spelExpressionParser.parseExpression(expr, ParserContext.TEMPLATE_EXPRESSION);

        //
        EvaluationContext ex = new StandardEvaluationContext();
        ex.setVariable("args", joinPoint.getArgs());

        return expression.getValue(ex, returnType);
    }
}
