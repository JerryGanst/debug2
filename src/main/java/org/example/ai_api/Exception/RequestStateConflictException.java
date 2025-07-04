package org.example.ai_api.Exception;

import org.example.ai_api.Bean.Enum.ResultCode;

/**
 * 409 资源状态与请求不符.
 */
public class RequestStateConflictException extends BaseException {
    public RequestStateConflictException() {
        super(ResultCode.CONFLICT, ResultCode.CONFLICT.getMessage());
    }

    public RequestStateConflictException(String message) {
        super(ResultCode.CONFLICT, message);
    }
}
