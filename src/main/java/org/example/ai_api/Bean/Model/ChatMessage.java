package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.Entity.FileUpload;

import java.util.List;

/**
 * 多轮对话单句信息实体类
 * @author 10353965
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ChatMessage {
    @JsonProperty("role")
    private String role;
    @JsonProperty("content")
    private String content;
    @JsonProperty("files")
    private List<FileUpload> uploads;
}
