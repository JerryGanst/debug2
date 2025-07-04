package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.Target;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TargetRepository extends MongoRepository<Target, String> {
    Target findByTargetName(@NotNull String targetName);
}
