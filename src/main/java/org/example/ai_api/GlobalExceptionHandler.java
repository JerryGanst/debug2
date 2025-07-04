package org.example.ai_api;

import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Exception.BaseException;
import org.example.ai_api.Exception.RateLimitException;
import org.example.ai_api.Exception.StreamApiException;
import org.example.ai_api.Exception.SyncApiException;
import org.example.ai_api.Utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import reactor.core.publisher.Flux;


/**
 * 全局异常处理.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class.getName());
    @Value("${base_package}")
    private String BASE_PACKAGE;

    /**
     * 处理自定义限流注解报错.
     *
     * @param ex      自定义异常
     * @param request 请求体
     * @return 根据请求类型返回不同数据
     */
    @ExceptionHandler(RateLimitException.class)
    public Object handleRateLimit(RateLimitException ex, WebRequest request) {
        // 创建统一错误响应结构
        ResultData<Void> errorResult = new ResultData<>(
                429,
                false,
                ex.getMessage(),
                null
        );
        // 判断请求类型
        if (Utils.isStreamRequest(request)) {
            // 流式响应：将错误包装成SSE事件
            return Flux.just(
                    ServerSentEvent.<ResultData<Void>>builder()
                            .event("error")
                            .data(errorResult)
                            .build()
            );
        } else {
            // 普通HTTP响应
            return ResponseEntity
                    .status(429)
                    .body(errorResult);
        }
    }

    /**
     * 处理文件上传超出大小限制的异常.
     *
     * @param e 文件上传超出大小限制异常
     * @return 包含错误信息的统一响应对象
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResultData<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        String errorDetail = getErrorDetail(e);
        return ResultData.fail(500, "上传文件大小单个不能超过50MB");
    }

    /**
     * 处理非流式接口报错的异常.
     *
     * @param ex 非流式抛出异常
     * @return 包含错误信息的统一响应对象
     */
    @ExceptionHandler(SyncApiException.class)
    public ResultData<String> handleSyncApiException(SyncApiException ex) {
        String errorDetail = getErrorDetail(ex);
        String message = ex.getMessage();
        if(ex.getCode() == 400){
            message = "文本过长，请重新尝试";
        }
        return ResultData.fail(ex.getCode(), message,errorDetail);
    }

    /**
     * 处理流式接口报错的异常.
     *
     * @param ex 流式抛出异常
     * @return 包含错误信息的统一响应对象
     */
    @ExceptionHandler(StreamApiException.class)
    public ResultData<String> handleStreamApiException(StreamApiException ex) {
        String errorDetail = getErrorDetail(ex);
        String message = ex.getMessage();
        if(ex.getCode() == 400){
            message = "文本过长，请重新尝试";
        }
        return ResultData.fail(ex.getCode(),message,errorDetail);
    }

    /**
     * 处理自定义异常.
     *
     * @param ex 自定义异常
     * @return 包含错误信息的统一响应对象
     */
    @ExceptionHandler(BaseException.class) // 捕获所有继承BaseException的异常
    public ResultData<String> handleCustomException(BaseException ex) {
        String errorDetail = getErrorDetail(ex);
        return ResultData.fail(ex.getErrorCode().getCode(), ex.getMessage(),errorDetail);
    }

    /**
     * 处理其他所有异常.
     *
     * @param ex 其他所有异常
     * @return 包含错误信息的统一响应对象
     */
    @ExceptionHandler(Exception.class)
    public ResultData<Exception> handleException(Exception ex) {
        String errorDetail = getErrorDetail(ex);
        // 返回包含详细信息的响应
        return new ResultData<>(
                500,
                false,
                "系统内部错误",
                ex
        );
    }

    /**
     * 获取异常的详细信息.
     * @param ex 异常对象
     * @return 异常的详细信息
     */
    private String getErrorDetail(Exception ex){
        // 获取完整的堆栈跟踪信息
        StackTraceElement[] stackTrace = ex.getStackTrace();
        // 寻找第一个属于项目代码的堆栈帧
        String errorDetail = "异常发生位置未知";
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith(BASE_PACKAGE)) {
                errorDetail = String.format("异常类型：%s,异常位置：%s.%s(%s:%d)",
                        ex.getClass().getSimpleName(),
                        element.getClassName(),
                        element.getMethodName(),
                        element.getFileName(),
                        element.getLineNumber());
                break;
            }
        }
        // 记录完整异常日志（包含堆栈跟踪）
        logger.error("Exception: {}", errorDetail, ex);
        return errorDetail;
    }
}