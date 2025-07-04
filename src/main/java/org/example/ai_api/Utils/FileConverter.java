package org.example.ai_api.Utils;

import org.apache.tika.exception.UnsupportedFormatException;
import org.example.ai_api.Strategy.Converter.ConverterContext;
import org.example.ai_api.Strategy.Converter.FileConvertStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档格式转换策略模式路由选择.
 */
@Component
public class FileConverter {
    private final List<FileConvertStrategy> strategies;

    @Autowired
    public FileConverter(List<FileConvertStrategy> strategies) {
        this.strategies = strategies;
    }

    public byte[] convert(MultipartFile file, String format) throws Exception {
        FileConvertStrategy strategy = findStrategy(format);
        ConverterContext context = new ConverterContext();
        return context.executeConversion(strategy, file);
    }

    private FileConvertStrategy findStrategy(String format) throws UnsupportedFormatException {
        return strategies.stream()
                .filter(s -> s.supports(format))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFormatException(format));
    }
}
