package org.example.ai_api.Exception;

import org.example.ai_api.Bean.Enum.ResultCode;

/**
 * 404 未找到资源.
 */
public class NotFoundException extends BaseException {
    public NotFoundException() {
        super(ResultCode.NOT_FOUND, ResultCode.NOT_FOUND.getMessage());
    }

    public NotFoundException(String message) {
        super(ResultCode.NOT_FOUND, message);
    }
}
