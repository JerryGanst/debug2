package org.example.ai_api.Bean.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class AgentConfig {
    /**
     * agent_name: 智能体名称
     */
    @JsonProperty("agent_name")
    private String agentName;
    /**
     * agent_role: 智能体角色
     */
    @JsonProperty("agent_role")
    private String agentRole;
    /**
     * agent_tone: 智能体语气
     */
    @JsonProperty("agent_tone")
    private String agentTone;
    /**
     * agent_description: 智能体描述
     */
    @JsonProperty("agent_description")
    private String agentDescription;
}
