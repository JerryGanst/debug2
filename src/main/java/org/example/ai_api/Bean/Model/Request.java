package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Request {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("question")
    private String question;
    @JsonProperty("model")
    private String model;
}
