package org.example.ai_api.Strategy.FileReader;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 默认读取器
 */
@Slf4j
@Component
@Order()
public class DefaultReader implements FileReaderStrategy{

    @Override
    public String read(InputStream inputStream) throws Exception {
        // 根据类型读取内容
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();
        // 自动检测文件类型并解析文本内容
        parser.parse(inputStream, handler, metadata, context);
        log.info("MIME类型: {}", metadata.get(Metadata.CONTENT_TYPE));
        return handler.toString();
    }

    @Override
    public Boolean support(String fileName) {
        return true;
    }
}
