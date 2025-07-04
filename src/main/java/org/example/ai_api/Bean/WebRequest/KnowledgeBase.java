package org.example.ai_api.Bean.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class KnowledgeBase<T> {
    @JsonProperty("file")
    List<T> file;
    @JsonProperty("userId")
    String userId;
    @JsonProperty("target")
    String target;
    @JsonProperty("isPublic")
    boolean isPublic;
    @JsonProperty("sortType")
    String sortType;
    @JsonProperty("increase")
    boolean increase;
    @JsonProperty("keywords")
    String keywords;
    @JsonProperty("page")
    int page = 1;
    @JsonProperty("pageSize")
    int pageSize = 10;
}
