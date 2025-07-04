package org.example.ai_api.Bean.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class PersonalRag {
    /**
     * 用户id
     */
    @JsonProperty("userId")
    private String userId;
    /**
     * 用户提问
     */
    @JsonProperty("question")
    private String question;
    /**
     * 模型选择
     */
    @JsonProperty("model")
    private String model;
}
