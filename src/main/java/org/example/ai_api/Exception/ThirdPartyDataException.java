package org.example.ai_api.Exception;

import org.example.ai_api.Bean.Enum.ResultCode;

/**
 * 523  第三方响应解析异常.
 */
public class ThirdPartyDataException extends BaseException {
    public ThirdPartyDataException() {
        super(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
    }

    public ThirdPartyDataException(String message) {
        super(ResultCode.INVALID_THIRD_PARTY_RESPONSE, message);
    }
}
