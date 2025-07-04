package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.SessionInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepository extends MongoRepository<SessionInfo,String> {
}
