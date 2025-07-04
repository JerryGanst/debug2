package org.example.ai_api.Bean.Model;

import lombok.*;

//时间与数据比率
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class DataRatio {
    private Double first_num;
    private Double second_num;
    private double ratio;
}
