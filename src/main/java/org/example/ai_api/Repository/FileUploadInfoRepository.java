package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.FileUpload;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileUploadInfoRepository extends MongoRepository<FileUpload, String> {

}
