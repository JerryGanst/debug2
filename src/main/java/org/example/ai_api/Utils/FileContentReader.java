package org.example.ai_api.Utils;

import org.example.ai_api.Strategy.FileReader.FileReaderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 文件文本提取(基于策略模式)
 */
@Component
public class FileContentReader {
    private final static Logger logger = LoggerFactory.getLogger(FileContentReader.class);
    private final List<FileReaderStrategy> strategies;

    // 通过构造函数注入所有策略实现
    @Autowired
    public FileContentReader(List<FileReaderStrategy> strategies) {
        // Spring会自动注入所有实现了FileReaderStrategy接口的bean
        this.strategies = strategies;
    }

    public String readFile(InputStream inputStream, String fileName) throws Exception {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        for (FileReaderStrategy strategy : strategies) {
            if (strategy.support(fileName)) {
                logger.info("使用{}策略读取文件", strategy.getClass().getSimpleName());
                return strategy.read(inputStream);
            }
        }
        throw new UnsupportedOperationException("不支持该文件格式");
    }
}
