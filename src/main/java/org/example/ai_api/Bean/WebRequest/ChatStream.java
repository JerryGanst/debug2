package org.example.ai_api.Bean.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.Model.ChatMessage;
import org.example.ai_api.Bean.Model.FileId;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ChatStream {
    /**
     * 对话内容
     */
    @JsonProperty("messages")
    private List<ChatMessage> messages;
    /**
     * 对话用户id
     */
    @JsonProperty("userId")
    private String userId;
    /**
     * 对话所选模型
     */
    @JsonProperty("model")
    private int model;
    @JsonProperty("isTemp")
    private boolean isTemp;
    /**
     * 对话附带文件信息
     */
    @JsonProperty("files")
    private List<FileId> fileIds;
}
