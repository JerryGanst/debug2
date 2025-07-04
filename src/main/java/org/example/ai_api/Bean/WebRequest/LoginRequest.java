package org.example.ai_api.Bean.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class LoginRequest {
    @JsonProperty("userid")
    private String userid;
    @JsonProperty("password")
    private String password;
    @JsonProperty("requestId")
    private String requestId;
}
