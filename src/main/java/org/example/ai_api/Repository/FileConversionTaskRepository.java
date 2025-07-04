package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.FileConversionTask;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileConversionTaskRepository extends MongoRepository<FileConversionTask, String> {
    FileConversionTask findByFileId(String fileId);
}
