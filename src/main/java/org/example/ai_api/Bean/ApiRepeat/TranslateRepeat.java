package org.example.ai_api.Bean.ApiRepeat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//翻译功能返回体
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class TranslateRepeat {
    /**
     * 翻译结果(流式)
     */
    @JsonProperty("content")
    private String translation_result;
    /**
     * 当前数据是否是结束数据
     */
    @JsonProperty("end")
    private boolean end;
}
