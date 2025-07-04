package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.RSAKeyPair;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RSAKeyPairRepository extends MongoRepository<RSAKeyPair, String> {
    RSAKeyPair findByRequestId(String requestId);
}
