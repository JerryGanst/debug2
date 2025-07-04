package org.example.ai_api.Bean.Model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class FileUploadResult {
    private String originalFileName;
    private String contentType;
    private byte[] fileBytes;
    private String fileId;
}
