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
public class Summary {
    @JsonProperty("question")
    private String question;
    @JsonProperty("user_id")
    private String user_id;
    @JsonProperty("file")
    private FileId file;

}
