package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 单次问答记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationMetrics {
    @JsonProperty("id")
    private String id;
    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][.SSS][.SS][.S]")
    private LocalDateTime startTime;
    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][.SSS][.SS][.S]")
    private LocalDateTime endTime;
    @JsonProperty("request")
    private Request request;
    @JsonProperty("question_category")
    private String questionCategory;
    @JsonProperty("final_answer")
    private FinalAnswer finalAnswer;
    @JsonProperty("domain")
    private String domain;
}
