package org.example.ai_api.Bean.ApiRepeat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

//文本总结返回体
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class SummaryRepeat {
    @JsonProperty("summary")
    private String summary;
    @JsonProperty("key_points")
    private List<String> key_points;

}
