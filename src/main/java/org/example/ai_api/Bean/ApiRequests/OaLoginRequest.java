package org.example.ai_api.Bean.ApiRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//登录请求体
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class OaLoginRequest {
    @JsonProperty("userid")
    private String userid;
    @JsonProperty("password")
    private String password;

}
