package org.example.ai_api.Bean.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("QAHistory")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Message<T,K> {
    @Id
    private String id;//数据库id定义
    @JsonProperty("userId")
    private String userid;//用户id(工号实现)
    @JsonProperty("type")
    private String type;//历史记录所属功能
    @JsonProperty("title")
    private String title;//历史记录标题(默认为用户的第一个问题)
    @JsonProperty("isThink")
    private Boolean isThink;//是否为思考状态
    private String date;//数据修改保存时间(工具类生成)
    private T data;//实际对话消息
    private  K feedback;//用户对于回答的评价

}
