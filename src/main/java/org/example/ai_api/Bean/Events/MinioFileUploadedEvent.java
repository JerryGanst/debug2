package org.example.ai_api.Bean.Events;

import lombok.*;
import org.example.ai_api.Bean.Entity.KnowledgeFileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class MinioFileUploadedEvent {
    private List<byte[]> files;
    private String userId;
    private String target;
    private boolean isPublic;
    private List<String> idsInSystem;
    private List<String> originalNames;
    private List<String> contentTypes;
}
