package org.example.ai_api.Exception;

import org.example.ai_api.Bean.Enum.ResultCode;

/**
 * 400 请求参数异常.
 */
public class BadRequestException extends BaseException {
    public BadRequestException() {
        super(ResultCode.BAD_REQUEST, ResultCode.BAD_REQUEST.getMessage());
    }

    public BadRequestException(String message) {
        super(ResultCode.BAD_REQUEST, message);
    }
}
