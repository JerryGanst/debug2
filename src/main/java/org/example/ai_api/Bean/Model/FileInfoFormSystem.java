package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfoFormSystem {
    @JsonProperty("ID")
    private String id;
    @JsonProperty("FileKey")
    private String fileKey;
    @JsonProperty("FatherID")
    private String fatherId;
    @JsonProperty("Category")
    private String Category;
    @JsonProperty("Url")
    private String url;
    @JsonProperty("Identical")
    private String identical;
    @JsonProperty("Level")
    private String level;
    @JsonProperty("CreateUser")
    private String createUser;
    @JsonProperty("CreateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime createTime;
}
