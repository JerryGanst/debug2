package org.example.ai_api.Controller;

import org.example.ai_api.Bean.Entity.Message;
import org.example.ai_api.Bean.WebRequest.FeedBack;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Exception.NotFoundException;
import org.example.ai_api.Service.MessageService;
import org.example.ai_api.Utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 关于历史记录的接口.
 *
 * @author 10353965
 */
@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping("/Message")
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class.getName());
    @Value("${categoriesKey}")
    private String categoriesKey;
    @Autowired
    private MessageService messageService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增聊天记录.
     *
     * @param message 新增的聊天记录信息
     * @return 保存结果
     */
    @PostMapping("/save")
    @ResponseBody
    public ResultData<String> insert(@RequestBody Message message) {
        logger.info("save message: {},{}", message.getUserid(),message.getType());
        if (message.getUserid() == null) {
            return ResultData.fail("信息用户id不可为空");
        }
        message.setDate(Utils.getNowDate());
        if (message.getId().isEmpty()) {
            message.setId(null);
        }
//           messageService.addTitleToRedis(message.getTitle(), message.getType());
        return ResultData.success("问答保存成功", messageService.save(message).getId());
    }

    /**
     * 针对聊天记录添加评价.
     *
     * @param feedBack 评价请求体
     * @return 保存结果
     */
    @PostMapping("/feedback")
    @ResponseBody
    public ResultData<String> feedback(@RequestBody FeedBack feedBack) {
        logger.info("feedback request: {}", feedBack.toString());
        Message message = messageService.findById(feedBack.getId());
        if (message == null) {
            return ResultData.fail("对应的消息记录不存在");
        }
        message.setFeedback(feedBack.getFeedback());
        messageService.save(message);
        return ResultData.success("评价成功");
    }

    /**
     * 根据用户id获得聊天记录.
     *
     * @param userId 用户id
     * @return 基于用户id获得的聊天记录
     */
    @PostMapping("/getMessageByUserId")
    @ResponseBody
    public ResultData<List<Message>> getMessageByUserId(@RequestParam("userId") String userId) {
        logger.info("getMessageByUserId: {}", userId);
        List<Message> list = messageService.findByUserid(userId);
        list = messageService.MessageSortByDate(list);
        return ResultData.success("查询成功", list);
    }

    /**
     * 根据id删除聊天记录.
     *
     * @param id 聊天记录id
     * @return 删除操作结果
     */
    @PostMapping("/deleteMessageById")
    @ResponseBody
    public ResultData<String> deleteMessageById(@RequestParam("id") String id) {
        logger.info("deleteMessageById: {}", id);
        messageService.deleteById(id);
        return ResultData.success("删除成功");
    }

    /**
     * 根据分类获得高频问答.
     *
     * @param type 类别
     * @return 根据类别获得的高频问答信息
     */
    @PostMapping("/getTopQuestion")
    @ResponseBody
    public ResultData<List<Object>> getTopQuestion(@RequestParam("type") String type) {
        logger.info("getTopQuestion: {}", type);
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet().reverseRangeWithScores("question:rank:" + type, 0, 4);
        assert tuples != null;
        List<Object> rank = tuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());
        return ResultData.success("获取成功", rank);
    }

    /**
     * 修改历史记录标题.
     *
     * @param title 修改后的标题
     * @param id    需要修改标题的历史记录的id
     * @return 修改结果
     */
    @PostMapping("changeTitle")
    @ResponseBody
    public ResultData<String> changeTitle(@RequestParam("title") String title, @RequestParam("id") String id) {
        logger.info("changeTitle: {}", id);
        Message message = messageService.findById(id);
        message.setTitle(title);
        messageService.save(message);
        return ResultData.success("修改成功");
    }

    /**
     * 根据历史记录id获得历史记录.
     *
     * @param id 历史记录id
     * @return 对应的历史记录
     */
    @PostMapping("getMessageById")
    @ResponseBody
    public ResultData<Message> getMessageById(@RequestParam("id") String id) {
        logger.info("getMessageById: {}", id);
        Message message = messageService.findById(id);
        if (message == null) {
            throw new NotFoundException("对应记录不存在");
        }
        return ResultData.success("查询成功", message);
    }

    /**
     * 获取某个时间段内的历史记录.
     *
     * @param userId    用户id
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 对应的历史记录
     */
    @PostMapping("/getMessageBetweenDate")
    @ResponseBody
    public ResultData<List<Message>> getMessageBetweenDate(@RequestParam("userId") String userId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        logger.info("getMessageBetweenDate: {} {} {}", startDate, endDate, userId);
        List<Message> list = messageService.findByUserIdAndDateBetween(userId, startDate, endDate);
        list = messageService.MessageSortByDate(list);
        return ResultData.success("查询成功", list);
    }

    /**
     * 获得时间点前的历史数据.
     *
     * @param userId    用户id
     * @param startDate 时间点
     * @return 对应的历史记录
     */
    @PostMapping("/getMessageBeforeDate")
    @ResponseBody
    public ResultData<List<Message>> getMessageBeforeDate(@RequestParam("userId") String userId, @RequestParam("startDate") String startDate) {
        logger.info("getMessageBeforeDate: {} {}", startDate, userId);
        List<Message> list = messageService.findByIdAndDateBefore(userId, startDate);
        list = messageService.MessageSortByDate(list);
        return ResultData.success("查询成功", list);
    }

    /**
     * 获得时间点后的历史数据.
     *
     * @param userId  用户id
     * @param endDate 时间点
     * @return 对应的历史记录
     */
    @PostMapping("/getMessageAfterDate")
    @ResponseBody
    public ResultData<List<Message>> getMessageAfterDate(@RequestParam("userId") String userId, @RequestParam("endDate") String endDate) {
        logger.info("getMessageAfterDate: {} {}", endDate, userId);
        List<Message> list = messageService.findByIdAndDateAfter(userId, endDate);
        list = messageService.MessageSortByDate(list);
        return ResultData.success("查询成功", list);
    }

    /**
     * 处理缓存中过量的问题记录.
     */
    @Scheduled(fixedRate = 60000) // 每1分钟执行一次
    public void batchTrimZSets() {
        logger.info("定时任务batchTrimZSets开始执行  {}", Utils.getNowDate());
        Set<String> categories = stringRedisTemplate.opsForSet().members(categoriesKey);
        int maxSize = 50;
        if (categories == null) {
            logger.warn("Redis键 {{}} 不存在或返回null", categoriesKey);
            return;
        }
        for (String category : categories) {
            String key = "question:rank:" + category;
            Long count = stringRedisTemplate.opsForZSet().size(key);
            if (count != null && count > maxSize) {
                stringRedisTemplate.opsForZSet().removeRange(key, 0, count - maxSize - 1);
            }
        }
        logger.info("定时任务batchTrimZSets执行结束  {}", Utils.getNowDate());
    }
}
