package org.example.ai_api.Exception;

import org.example.ai_api.Bean.Enum.ResultCode;


/**
 * 422  数据不符合业务规定.
 */
public class DataNotComplianceException extends BaseException {

    public DataNotComplianceException() {
        super(ResultCode.DATA_NOT_COMPLIANCE, ResultCode.DATA_NOT_COMPLIANCE.getMessage());
    }

    public DataNotComplianceException(String message) {
        super(ResultCode.DATA_NOT_COMPLIANCE, message);
    }
}
