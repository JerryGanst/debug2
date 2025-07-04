package org.example.ai_api.Bean.ApiRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class SummarizeRequest {
    @JsonProperty("question")
    private String question;
    @JsonProperty("user_id")
    private String user_id;

}
