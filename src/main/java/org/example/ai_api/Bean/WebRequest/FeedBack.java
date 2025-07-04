package org.example.ai_api.Bean.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class FeedBack<T> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("feedback")
    private T feedback;

}
