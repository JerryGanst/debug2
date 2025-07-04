package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.FileIdData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileIdDataRepository extends MongoRepository<FileIdData, String> {
    FileIdData findByFileIdInSystem(String fileId);
}
