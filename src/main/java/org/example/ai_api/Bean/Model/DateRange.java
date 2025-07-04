package org.example.ai_api.Bean.Model;

import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class DateRange {
    private Instant start;
    private Instant end;
}
