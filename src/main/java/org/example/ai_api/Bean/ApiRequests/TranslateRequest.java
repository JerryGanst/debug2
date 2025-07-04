package org.example.ai_api.Bean.ApiRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//翻译功能请求体
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class TranslateRequest {
    @JsonProperty("user_id")
    private String user_id;
    @JsonProperty("target_language")
    private String target_language;
    @JsonProperty("source_text")
    private String source_text;
//    @JsonProperty("file_type")
//    private String file_type;

}
