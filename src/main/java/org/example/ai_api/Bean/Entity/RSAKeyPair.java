package org.example.ai_api.Bean.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Document(collection = "RSAKeyPair")
@Data
@AllArgsConstructor
@Setter
@Getter
@ToString
public class RSAKeyPair {
    @Id
    private String id; // MongoDB自动ID

    private String requestId; // 密钥对唯一标识
    private String publicKey; // PEM格式的公钥
    private byte[] privateKey; // 私钥字节数组
    private Instant createdAt; // 创建时间
    private Instant expiresAt; // 过期时间

    // 构造函数
    public RSAKeyPair() {
        this.requestId = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);
    }
}
