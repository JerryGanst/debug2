package org.example.ai_api.Exception;

import org.example.ai_api.Bean.Enum.ResultCode;

/**
 * 503  第三方服务不可用.
 */
public class ThirdServiceException extends BaseException {
    public ThirdServiceException(String message) {
        super(ResultCode.THIRD_PARTY_ERROR, message);
    }
}
