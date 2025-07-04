package org.example.ai_api.Strategy.Converter;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件格式转换策略模式上下文，实现转换与转换可能需要的前后置操作.
 */
public class ConverterContext {
    public byte[] executeConversion(FileConvertStrategy strategy, MultipartFile file) throws Exception {
        return strategy.execute(file);
    }
}
