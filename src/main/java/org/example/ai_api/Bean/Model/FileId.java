package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class FileId {
    /**
     * 文件id
     */
    @JsonProperty("fileId")
    String fileId;
    /**
     * 是否是本地文件(知识库文件时为假)
     */
    @JsonProperty("local")
    boolean isLocal = true;
}
