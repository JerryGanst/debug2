package org.example.ai_api.Bean.ApiRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class LuxlinkLogin {
    @JsonProperty("access_key")
    private String access_key;
    @JsonProperty("tokenId")
    private String tokenId;
    @Value("${cmd}")
    private String cmd;

}
