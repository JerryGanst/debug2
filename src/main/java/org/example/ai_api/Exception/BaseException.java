package org.example.ai_api.Exception;

import org.example.ai_api.Bean.Enum.ResultCode;

/**
 * 自定义基类异常.
 */
public abstract class BaseException extends RuntimeException {
    private final ResultCode errorCode; // 自定义错误码枚举

    public BaseException(ResultCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    // 获取错误码
    public ResultCode getErrorCode() {
        return errorCode;
    }
}
