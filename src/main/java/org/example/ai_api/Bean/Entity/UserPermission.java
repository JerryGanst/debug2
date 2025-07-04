package org.example.ai_api.Bean.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("UserPermission")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class UserPermission {
    @Id
    private String id;
    private String userId;
    private String target;
    private boolean delete;
    private boolean upload;
    private boolean read = true;
}
