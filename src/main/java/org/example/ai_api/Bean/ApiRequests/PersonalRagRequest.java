package org.example.ai_api.Bean.ApiRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.WebRequest.PersonalRag;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class PersonalRagRequest {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("question")
    private String question;
    @JsonProperty("model")
    private String model;
    @JsonProperty("objects")
    private List<String> filePath;

    public PersonalRagRequest(PersonalRag personalRag){
        this.userId = personalRag.getUserId();
        this.question = personalRag.getQuestion();
    }
}