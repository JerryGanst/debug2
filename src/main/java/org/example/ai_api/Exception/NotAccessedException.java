package org.example.ai_api.Exception;

import org.example.ai_api.Bean.Enum.ResultCode;

/**
 * 401 未授权.
 */
public class NotAccessedException extends BaseException {
    public NotAccessedException() {
        super(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
    }

    public NotAccessedException(String message) {
        super(ResultCode.UNAUTHORIZED, message);
    }
}
