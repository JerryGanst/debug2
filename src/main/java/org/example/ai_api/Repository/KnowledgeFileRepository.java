package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.KnowledgeFileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface KnowledgeFileRepository extends MongoRepository<KnowledgeFileInfo, String> {
    List<KnowledgeFileInfo> findByUploaderIdAndIsPublic(String uploaderId, boolean isPublic);
    List<KnowledgeFileInfo> findByFileTargetAndIsPublic(String fileTarget, boolean isPublic);
    List<KnowledgeFileInfo> findByFileTargetAndIsPublicAndFileNameIn(String fileTarget, boolean isPublic,List<String> fileNames);
    List<KnowledgeFileInfo> findByUploaderIdAndIsPublicAndFileNameIn(String uploaderId, boolean isPublic,List<String> fileNames);
    List<KnowledgeFileInfo> findByFileTargetAndIsPublicAndHashCodeIn(String fileTarget, boolean isPublic,List<String> hashCodes);
    List<KnowledgeFileInfo> findByUploaderIdAndIsPublicAndHashCodeIn(String uploaderId, boolean isPublic,List<String> hashCodes);
}
