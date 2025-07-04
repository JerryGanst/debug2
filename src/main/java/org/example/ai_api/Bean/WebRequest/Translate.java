package org.example.ai_api.Bean.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.Model.FileId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Translate{
    @JsonProperty("user_id")
    private String user_id;
    @JsonProperty("target_language")
    private String target_language;
    @JsonProperty("source_text")
    private String source_text;
    @JsonProperty("file")
    private FileId file;
}
