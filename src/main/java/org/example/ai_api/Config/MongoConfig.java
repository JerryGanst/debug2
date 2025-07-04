package org.example.ai_api.Config;

import org.example.ai_api.Bean.Entity.UserLoginAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
public class MongoConfig {

    @Autowired
    public void initIndexes(MongoTemplate mongoTemplate) {
        // 1. 获取指定集合的索引操作对象
        IndexOperations indexOps = mongoTemplate.indexOps(UserLoginAttempt.class);
        // 2. 检查索引是否已存在
        if (!indexExists(indexOps, "expireAt")) {
            // 3. 创建TTL索引
            indexOps.ensureIndex(
                    // 4. 定义索引
                    new Index().on("expireAt", Sort.Direction.ASC)  // 5. 指定索引字段和排序方向
                            .expire(0L)  // 6. 设置TTL过期行为
            );
        }
    }

    // 7. 辅助方法：检查索引是否存在
    private boolean indexExists(IndexOperations indexOps, String fieldName) {
        return indexOps.getIndexInfo().stream()
                .anyMatch(index -> index.getIndexFields().stream()
                        .anyMatch(field -> field.getKey().equals(fieldName)));
    }
}
