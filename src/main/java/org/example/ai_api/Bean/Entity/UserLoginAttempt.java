package org.example.ai_api.Bean.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "UserLoginAttempt")
public class UserLoginAttempt {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;       // 用户ID
    private int count = 0;      // 失败次数
    private Date lockUntil;     // 锁定截止时间
    private Date lastAttemptTime; // 最近尝试时间
    private Date expireAt;       // 自动过期时间
    private Date createTime = new Date();     // 创建时间
}