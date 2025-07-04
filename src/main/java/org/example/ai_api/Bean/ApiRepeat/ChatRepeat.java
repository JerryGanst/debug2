package org.example.ai_api.Bean.ApiRepeat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.Model.ChatMessage;
import org.example.ai_api.Bean.Model.Usage;

//多轮对话接口返回结果(非流式)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ChatRepeat {
    @JsonProperty("message")
    private ChatMessage message;
    @JsonProperty("usage")
    private Usage usage;
}
