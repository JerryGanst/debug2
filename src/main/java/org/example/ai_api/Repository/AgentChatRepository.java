package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.AgentChatInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AgentChatRepository extends MongoRepository<AgentChatInfo,String> {
    List<AgentChatInfo> findAgentChatsByAgentIdAndUserId(String agentId, String userId);
    void deleteByAgentId(String agentId);
}
