package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Persona {
    /**
     * 智能体名称
     */
    private String name;
    /**
     * 智能体角色
     */
    private String role;
    /**
     * 智能体对话语气
     */
    private String tone;
    /**
     * 智能体整体描述
     */
    private String description;
}
