package org.example.ai_api.Aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.ai_api.Annotation.RateLimiter;
import org.example.ai_api.Exception.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 基于redis实现对AI接口的限流
 */
@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class.getName());
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_SCRIPT =
            "local count = redis.call('INCR', KEYS[1])\n" +
                    "if count == 1 then\n" +
                    "    redis.call('EXPIRE', KEYS[1], ARGV[1])\n" +
                    "end\n" +
                    "return count";

    @Around("@annotation(rateLimiter)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = signature.getMethod().getDeclaringClass().getName();

        // 生成基于方法和时间窗口的Redis键
        String key = String.format("rate_limit:%s", className);

        // 限流参数
        int limit = rateLimiter.value();
        int expire = rateLimiter.expire();

        // 执行Lua脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class);
        Long count = redisTemplate.execute(redisScript, Collections.singletonList(key), expire);
        logger.info("count:{},limit:{}", count, limit);
        if (count != null && count > limit) {
            // 判断方法返回类型
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Class<?> returnType = method.getReturnType();

            // 针对Flux/Mono返回类型特殊处理
            if (Flux.class.isAssignableFrom(returnType)) {
                return createRateLimitFluxResponse();
            } else if (Mono.class.isAssignableFrom(returnType)) {
                return createRateLimitMonoResponse();
            } else {
                throw new RateLimitException("请求过于频繁");
            }
        }

        return joinPoint.proceed();
    }

    private Flux<?> createRateLimitFluxResponse() {
        return Flux.just(
                ServerSentEvent.builder()
                        .event("error")
                        .data(Collections.singletonMap("code", 429))
                        .build()
        ).concatWith(Flux.error(new RateLimitException("请求过于频繁")));
    }

    private Mono<?> createRateLimitMonoResponse() {
        return Mono.error(new RateLimitException("请求过于频繁"));
    }
}
