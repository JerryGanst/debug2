package org.example.ai_api.Exception;

/**
 * 自定义异常，AOP拦截请求过多时触发.
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}