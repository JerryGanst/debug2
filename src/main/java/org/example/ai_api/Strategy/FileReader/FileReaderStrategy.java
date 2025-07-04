package org.example.ai_api.Strategy.FileReader;

import java.io.InputStream;

/**
 * 文件文本读取策略接口
 */
public interface FileReaderStrategy {
    String read(InputStream inputStream) throws Exception;
    Boolean support(String fileName);
}
