package org.example.ai_api.Bean.ApiRepeat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//oa登录接口返回体
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class LoginRepeat {
    @JsonProperty("status")
    private String status;
    @JsonProperty("clientStatus")
    private String clientStatus;
    @JsonProperty("message")
    private String message;

}
