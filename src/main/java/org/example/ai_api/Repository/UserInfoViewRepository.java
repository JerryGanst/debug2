package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.UserInfo;
import org.springframework.data.repository.Repository;

public interface UserInfoViewRepository extends Repository<UserInfo, Long> {
    UserInfo findById(String id);
}
