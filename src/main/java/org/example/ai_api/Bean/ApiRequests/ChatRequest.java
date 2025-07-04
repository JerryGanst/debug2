package org.example.ai_api.Bean.ApiRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.Model.ChatMessage;

import java.util.List;

//多轮对话请求体
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ChatRequest {
    @JsonProperty("messages")
    private List<ChatMessage> messages;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("model")
    private String model;
    @JsonProperty("file")
    private List<String> file;
    private boolean stream = false;
}
