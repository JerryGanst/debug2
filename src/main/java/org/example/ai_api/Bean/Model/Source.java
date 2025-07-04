package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//单轮问答文件信息
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Source {
    @JsonProperty("document_id")
    private String document_id;
    @JsonProperty("document_title")
    private String document_title;
    private int page;
    private String text;
    private double score;

}
