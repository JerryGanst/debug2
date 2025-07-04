package org.example.ai_api.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ContentTypeDetector {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    static {
        // 图片类型
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("bmp", "image/bmp");

        // 文档类型
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("csv", "text/csv");

        // 压缩文件
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("rar", "application/x-rar-compressed");
        MIME_TYPES.put("7z", "application/x-7z-compressed");
        MIME_TYPES.put("tar", "application/x-tar");
        MIME_TYPES.put("gz", "application/gzip");

        // 音视频
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("avi", "video/x-msvideo");
        MIME_TYPES.put("mov", "video/quicktime");
        MIME_TYPES.put("mkv", "video/x-matroska");

        // 其他
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("js", "text/javascript");
        MIME_TYPES.put("css", "text/css");
    }

    /**
     * 根据文件名获取Content-Type
     *
     * @param fileName 完整文件名（如：example.jpg）
     * @return 对应的MIME类型，未知类型返回默认值
     */
    public static String getContentType(String fileName) {
        return Optional.ofNullable(fileName)
                .map(f -> {
                    // 处理带有点号的文件名（如.tar.gz）
                    int lastDotIndex = f.lastIndexOf('.');
                    return (lastDotIndex != -1) ? f.substring(lastDotIndex + 1) : "";
                })
                .map(ext -> ext.toLowerCase().trim())
                .filter(ext -> !ext.isEmpty())
                .map(ext -> MIME_TYPES.getOrDefault(ext, DEFAULT_CONTENT_TYPE))
                .orElse(DEFAULT_CONTENT_TYPE);
    }

    /**
     * 添加自定义MIME类型映射
     *
     * @param extension 文件扩展名（不带点）
     * @param mimeType  MIME类型
     */
    public static void addCustomMimeType(String extension, String mimeType) {
        MIME_TYPES.put(extension.toLowerCase(), mimeType);
    }
}
