package org.example.ai_api.Bean.Model;

import lombok.*;
import org.example.ai_api.Bean.Enum.ResultCode;

//返回结果统一格式
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResultData<T> {

    private Integer code;

    private Boolean status;

    private String message;

    private T data;

    private static <T> ResultData<T> response(Integer code, Boolean status, String message, T data) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(code);
        resultData.setStatus(status);
        resultData.setMessage(message);
        resultData.setData(data);
        return resultData;
    }

    private static <T> ResultData<T> response(Integer code, Boolean status, String message) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(code);
        resultData.setStatus(status);
        resultData.setMessage(message);
        return resultData;
    }

    public static <T> ResultData<T> success() {
        return response(ResultCode.SUCCESS.getCode(), true, ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> ResultData<T> success(ResultCode httpResponseEnum) {
        return response(httpResponseEnum.getCode(), true, httpResponseEnum.getMessage());
    }

    public static <T> ResultData<T> success(Integer code, String message) {
        return response(code, true, message);
    }

    public static <T> ResultData<T> success(String message, T data) {
        return response(ResultCode.SUCCESS.getCode(), true, message, data);
    }

    public static <T> ResultData<T> success(Integer code, String message, T data) {
        return response(code, true, message, data);
    }

    public static <T> ResultData<T> success(T data) {
        return response(ResultCode.SUCCESS.getCode(), true, ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResultData<T> success(String message) {
        return response(ResultCode.SUCCESS.getCode(), true, message, null);
    }

    public static <T> ResultData<T> fail() {
        return response(ResultCode.ERROR.getCode(), false, ResultCode.ERROR.getMessage(), null);
    }

    public static <T> ResultData<T> fail(ResultCode httpResponseEnum) {
        return response(httpResponseEnum.getCode(), false, httpResponseEnum.getMessage());
    }

    public static <T> ResultData<T> fail(Integer code, String message) {
        return response(code, false, message);
    }

    public static <T> ResultData<T> fail(String message, T data) {
        return response(ResultCode.ERROR.getCode(), false, message, data);
    }

    public static <T> ResultData<T> fail(Integer code, String message, T data) {
        return response(code, false, message, data);
    }

    public static <T> ResultData<T> fail(T data) {
        return response(ResultCode.ERROR.getCode(), false, ResultCode.ERROR.getMessage(), data);
    }

    public static <T> ResultData<T> fail(String message) {
        return response(ResultCode.ERROR.getCode(), false, message, null);
    }


}
