package org.example.ai_api.Bean.Events;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class MinioFileDeleteEvent {
    private String userId;
    private String fileId;
    private String target;
}