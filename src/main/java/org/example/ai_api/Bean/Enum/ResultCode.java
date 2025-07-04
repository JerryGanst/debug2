package org.example.ai_api.Bean.Enum;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数列表错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "访问受限，授权过期"),
    NOT_FOUND(404, "资源，服务未找！"),
    CONFLICT(409, "请求与资源状态不符"),
    DATA_NOT_COMPLIANCE(422, "数据不符合业务规定"),
    TOO_MANY_REQUESTS(429,"当前请求过多，稍后重试"),
    INVALID_THIRD_PARTY_RESPONSE(523, "第三方响应数据异常"),
    ERROR(500, "系统内部错误"),
    THIRD_PARTY_ERROR(503, "第三方服务不可用"),
    WARN(601,"系统警告消息");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
