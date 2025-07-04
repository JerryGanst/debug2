package org.example.ai_api.Repository;

import org.example.ai_api.Bean.Entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistoryRepository extends MongoRepository<Message,String> {
    List<Message> findByUserid(String userId);
    List<Message> findByUseridAndDateAfter(String userId,String date);
    List<Message> findByUseridAndDateBefore(String userId,String date);
    List<Message> findByUseridAndDateBetween(String userId,String date1,String date2);
}
