package org.example.ai_api.Utils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SelfCleanFileResource extends FileSystemResource {
    private static final Logger logger = LoggerFactory.getLogger(SelfCleanFileResource.class);
    private final Path filePath;

    public SelfCleanFileResource(File file) {
        super(file);
        this.filePath = file.toPath();
    }

    @NotNull
    @Override
    public InputStream getInputStream() throws IOException {
        return new FilterInputStream(super.getInputStream()) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    // 流关闭时立即删除文件
                    Files.deleteIfExists(filePath);
                    logger.info("临时文件已删除{}", filePath);
                }
            }
        };
    }
}
