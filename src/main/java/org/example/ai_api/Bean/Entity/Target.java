package org.example.ai_api.Bean.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Document(collection = "Target")
public class Target {
    @Id
    private String id;
    private String targetName;
    private boolean delete;
    private boolean upload;
    private boolean read;
    private String category;
    private String cid;
    private String dir;
}
