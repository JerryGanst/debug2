package org.example.ai_api.Bean.ApiRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.Model.AgentConfig;
import org.example.ai_api.Bean.Model.ChatMessage;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
//智能体对话请求体
public class AgentChatRequest {
    @JsonProperty("messages")
    private List<ChatMessage> messages;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("model")
    private String model;
    @JsonProperty("file")
    private List<String> file;
    @JsonProperty("agent_config")
    private AgentConfig agentConfig;
    private boolean stream = false;
}
