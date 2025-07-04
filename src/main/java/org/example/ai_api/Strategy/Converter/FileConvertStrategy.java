package org.example.ai_api.Strategy.Converter;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件格式转换策略模式接口.
 */
public interface FileConvertStrategy {
    byte[] execute(MultipartFile file) throws Exception;
    boolean supports(String format);
}
