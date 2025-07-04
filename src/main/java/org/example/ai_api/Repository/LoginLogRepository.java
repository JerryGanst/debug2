package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.LoginLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoginLogRepository extends MongoRepository<LoginLog, String> {
    LoginLog findByUserId(String userId);
}
