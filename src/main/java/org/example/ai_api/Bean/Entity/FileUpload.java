package org.example.ai_api.Bean.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("FileUpload")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class FileUpload {
    @Id
    private String fileId;
    @JsonProperty("originalFileName")
    private String originalFileName;
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("filePath")
    private String filePath;
    @JsonProperty("fileType")
    private String fileType;
    @JsonProperty("uploadTime")
    private String uploadTime;
    @JsonProperty("local")
    private boolean isLocal = true;
}
