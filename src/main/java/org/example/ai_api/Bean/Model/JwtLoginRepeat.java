package org.example.ai_api.Bean.Model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class JwtLoginRepeat {
    /**
     * 短期token(1小时)
     */
    private String jwtToken;
    /**
     * 长期token(7天)
     */
    private String refreshToken;
    /**
     * 登录结果数据
     */
    private Object data;
}
