package org.example.ai_api.Bean.ApiRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class SimilarityRequest {
    @JsonProperty("str1")
    private String str1;
    @JsonProperty("str2")
    private String str2;

}
