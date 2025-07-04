package org.example.ai_api.Bean.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

//文件相关实体类
@Document("Files")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class FileInfo {
    @Id
    private String id;//文件信息数据库id
    @JsonProperty("fileName")
    @Field("file_name")
    private String fileName;//文件名
    @JsonProperty("fileLink")
    @Field("file_link")
    private String fileLink;//文件查看连接
}
