package org.example.ai_api.Bean.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.Model.ChatMessage;
import org.example.ai_api.Bean.Model.FileId;

import java.util.List;

//智能体对话请求
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class AgentChat {
    /**
     * 对话所选智能体id
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * 对话用户id
     */
    @JsonProperty("userId")
    private String userId;
    /**
     * 对话内容
     */
    @JsonProperty("messages")
    private List<ChatMessage> messages;
    /**
     * 对话所选模型
     */
    @JsonProperty("model")
    private int model;
    /**
     * 对话附带文件信息
     */
    @JsonProperty("files")
    private List<FileId> fileIds;
}
