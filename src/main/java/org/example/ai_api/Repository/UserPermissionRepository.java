package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.UserPermission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserPermissionRepository extends MongoRepository<UserPermission, String> {
    List<UserPermission> findUserPermissionByUserId(String userId);
    void removeUserPermissionByUserIdAndTarget(String userId, String target);
    UserPermission findUserPermissionByUserIdAndTarget(String userId, String target);
}
