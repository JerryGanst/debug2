package org.example.ai_api.Strategy.Converter;

import org.example.ai_api.Utils.SelfCleanFileResource;
import org.example.ai_api.Utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

/**
 * 文件格式转换策略模式具体实现 - 转换为pdf.
 */
@Component
public class PdfConverterStrategy implements FileConvertStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PdfConverterStrategy.class.getName());

    @Autowired
    private RestTemplate restTemplate;
    @Value("${libreoffice_convert}")
    private String convertUrl;
    @Value("${libreoffice_ready}")
    private String readyUrl;

    @Override
    public byte[] execute(MultipartFile file) throws Exception {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), "."+Utils.getFileExtension(Objects.requireNonNull(file.getOriginalFilename())));
        try{
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            file.transferTo(tempFile);
            FileSystemResource fileSystemResource = new SelfCleanFileResource(tempFile);
            body.add("file", fileSystemResource); // 参数名必须与 @RequestParam("file") 对应
            body.add("format","pdf");
            return Utils.LibreOfficeFileConverter(body, restTemplate, readyUrl, convertUrl, logger);
        } catch (Exception e) {
            Files.deleteIfExists(tempFile.toPath());
            throw e;
        }finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    @Override
    public boolean supports(String format) {
        return "pdf".equalsIgnoreCase(format);
    }
}
