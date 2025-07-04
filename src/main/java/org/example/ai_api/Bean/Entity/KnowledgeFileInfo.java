package org.example.ai_api.Bean.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document("KnowledgeFile")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@CompoundIndex(name = "public_file_index", def = "{'isPublic': 1, 'fileTarget': 1, 'fileName': 1}")
@CompoundIndex(name = "private_file_index", def = "{'isPublic': 1, 'uploaderId': 1, 'fileName': 1}")
public class KnowledgeFileInfo {
    @Id
    private String id;
    private String originalFileName;
    private String fileName;
    private String storagePath;
    private String convertPath;
    private String fileType;
    private String fileTarget;
    private long fileSize;
    private String uploaderId;
    @Field("isPublic")
    private boolean isPublic;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String hashCode;
}
