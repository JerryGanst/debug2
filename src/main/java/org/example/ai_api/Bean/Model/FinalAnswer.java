package org.example.ai_api.Bean.Model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class FinalAnswer {
    @JsonProperty("is_question_answered")
    private boolean isQuestionAnswered;
    @JsonProperty("answer")
    private String answer;
    @JsonProperty("contexts")
    private List<Source> contexts;
}
