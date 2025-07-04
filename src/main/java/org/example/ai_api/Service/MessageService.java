package org.example.ai_api.Service;

import org.example.ai_api.Bean.Entity.Message;
import org.example.ai_api.Bean.ApiRepeat.SimilarityRepeat;
import org.example.ai_api.Bean.ApiRequests.SimilarityRequest;
import org.example.ai_api.Config.AIConfig;
import org.example.ai_api.Exception.NotFoundException;
import org.example.ai_api.Exception.ThirdPartyDataException;
import org.example.ai_api.Repository.HistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * 聊天历史记录相关服务.
 */
@Service
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class.getName());
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AIConfig aiConfig;
    @Value("${targetScore}")
    private double targetScore;
    @Value("${categoriesKey}")
    private String categoriesKey;
    @Value("${rank_key}")
    private String rankKey;

    /**
     * 根据用户id返回历史记录.
     *
     * @param userId 用户id
     * @return 历史记录链表
     */
    public List<Message> findByUserid(String userId) {
        logger.info("根据用户id获得历史记录: {}", userId);
        return historyRepository.findByUserid(userId);
    }

    /**
     * 保存历史记录.
     *
     * @param message 需要保存的记录
     * @return 保存后的信息结构
     */
    public Message save(Message message) {
        logger.info("保存历史记录,用户id: {}", message.getUserid());
        return historyRepository.save(message);
    }

    /**
     * 根据id获得记录.
     *
     * @param id 历史记录id
     * @return 查询的对应历史记录
     */
    public Message findById(String id) {
        logger.info("根据记录id获得历史记录: {}", id);
        return historyRepository.findById(id).orElseThrow(() -> new NotFoundException("未找到历史记录"));
    }

    /**
     * 根据id删除历史记录.
     *
     * @param id 历史记录id
     */
    public void deleteById(String id) {
        logger.info("根据记录id删除历史记录: {}", id);
        historyRepository.deleteById(id);
    }

    public List<Message> findByUserIdAndDateBetween(String userid, String startDate, String endDate) {
        logger.info("根据用户id和时间段获得历史记录: {} {} {}", userid, startDate, endDate);
        return historyRepository.findByUseridAndDateBetween(userid, startDate, endDate);
    }

    public List<Message> findByIdAndDateBefore(String id, String startDate) {
        logger.info("根据记录id和时间获得时间点前的历史记录: {} {}", id, startDate);
        return historyRepository.findByUseridAndDateBefore(id, startDate);
    }

    public List<Message> findByIdAndDateAfter(String id, String endDate) {
        logger.info("根据记录id和时间获得时间点后的历史记录: {} {}", id, endDate);
        return historyRepository.findByUseridAndDateAfter(id, endDate);
    }

    /**
     * 将问题添加到redis缓存.
     *
     * @param title 问题内容
     * @param type  问题所属分类
     */
    @Async("taskExecutor")
    public void addTitleToRedis(String title, String type) {
        Set<String> set = stringRedisTemplate.opsForZSet().range(rankKey + type, 0, -1);
        //若set为空，表示当前分类不存在，直接保存即可
        if (set == null || set.isEmpty()) {
            stringRedisTemplate.opsForSet().add(categoriesKey, type);
            stringRedisTemplate.opsForZSet().incrementScore(rankKey + type, title, 1);
            return;
        }
        boolean flag = true;
        double nowScore = 0.0;
        String nowTitle = "";
        WebClient webClient = WebClient.builder()
                .defaultHeader("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(aiConfig.getCategories().get("similarity"))
                .build();
        //set不为空，计算后根据阈值判断是否添加
        for (String value : set) {
            SimilarityRequest similarityRequest = new SimilarityRequest(value, title);
            double score = webClient.post()
                    .body(BodyInserters.fromValue(similarityRequest))
                    .retrieve()
                    .bodyToMono(SimilarityRepeat.class)
                    .blockOptional()
                    .orElseThrow(() -> new ThirdPartyDataException("返回体为空"))
                    .getScore();
            logger.info("similarityRequest:{}", similarityRequest);
            logger.info("score: {}", score);
            if (score > targetScore) {
                flag = false;
                if (score > nowScore) {
                    nowScore = score;
                    nowTitle = value;
                }
            }
        }
        if (flag) {
            stringRedisTemplate.opsForSet().add(categoriesKey, type);
            stringRedisTemplate.opsForZSet().incrementScore(rankKey + type, title, 1);
        } else {
            stringRedisTemplate.opsForZSet().incrementScore(rankKey + type, nowTitle, 1);
        }
    }

    public List<Message> MessageSortByDate(List<Message> list) {
        logger.info("对历史记录进行排序");
        list.sort((o1, o2) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime date1 = LocalDateTime.parse(o1.getDate(), formatter);
            LocalDateTime date2 = LocalDateTime.parse(o2.getDate(), formatter);
            return date2.compareTo(date1);
        });
        return list;
    }
}
