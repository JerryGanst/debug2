package org.example.ai_api.Bean.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//知识库问答请求体
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Query {
    @JsonProperty("type")
    private String type;
    @JsonProperty("question")
    private String question;
    @JsonProperty("user_id")
    private String user_id;
    @JsonProperty("model")
    private int model;
}
