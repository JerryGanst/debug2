package org.example.ai_api.Bean.Entity;

import lombok.*;
import org.example.ai_api.Bean.Model.ChatMessage;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "AgentChat")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class AgentChatInfo<T> {
    /**
     * 对话记录id
     */
    @Id
    private String id;
    /**
     * 智能体id
     */
    private String agentId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 对话标题
     */
    private String title;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 对话内容
     */
    private List<ChatMessage> messages;
    /**
     * 用户反馈
     */
    private T feedback;
}
