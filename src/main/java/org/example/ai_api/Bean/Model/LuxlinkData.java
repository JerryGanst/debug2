package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class LuxlinkData {
    @JsonProperty("uid")
    private String uid;
    @JsonProperty("tokenId")
    private String tokenId;
    @JsonProperty("access_key")
    private String access_key;
    @JsonProperty("validate")
    private boolean validate;
}
