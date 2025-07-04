package org.example.ai_api.Bean.ApiRepeat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.ai_api.Bean.Model.LuxlinkData;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class LuxlinkRepeat {
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("result")
    private String result;
    @JsonProperty("id")
    private String id;
    @JsonProperty("data")
    private LuxlinkData data;

}
