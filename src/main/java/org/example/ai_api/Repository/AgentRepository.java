package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.Agent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AgentRepository extends MongoRepository<Agent,String> {
    List<Agent> findAgentsByUserId(String userId);
}
