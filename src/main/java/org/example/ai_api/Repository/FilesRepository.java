package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.FileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;


//文件数据库接口
public interface FilesRepository extends MongoRepository<FileInfo, String> {
    //根据文件名获得文件信息
    FileInfo findByFileName(String fileName);

}
