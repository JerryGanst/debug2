package org.example.ai_api.Bean.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "FileConversionTask")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@CompoundIndexes({
        @CompoundIndex(name = "status_createTime_idx", def = "{'status': 1, 'createTime': 1}"),
        @CompoundIndex(name = "priority_idx", def = "{'priority': -1}") // 优先级索引
})
public class FileConversionTask {
    @Id
    private String taskId;
    private String fileId;           // 关联文件ID
    private String targetFormat;     // 目标格式（e.g. "pdf"）
    private String status;           // PENDING/PROCESSING/COMPLETED/FAILED
    private int retryCount = 0;      // 重试次数（默认0）
    private int maxRetries = 3;      // 最大重试次数
    private String errorMessage;     // 错误详情
    private String convertedFilePath;  // 转换后文件路径
    private int priority = 5;        // 优先级（1-10，默认5）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}