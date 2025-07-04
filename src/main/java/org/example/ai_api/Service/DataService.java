package org.example.ai_api.Service;

import org.example.ai_api.Bean.Entity.SessionInfo;
import org.example.ai_api.Bean.Entity.UserInfo;
import org.example.ai_api.Bean.Model.DataRatio;
import org.example.ai_api.Bean.Model.DateRange;
import org.example.ai_api.Bean.Model.Kmeans;
import org.example.ai_api.Bean.Model.OperationMetrics;
import org.example.ai_api.Repository.SessionRepository;
import org.example.ai_api.Repository.UserInfoViewRepository;
import org.example.ai_api.Utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataService {
    private static final Logger logger = LoggerFactory.getLogger(DataService.class.getName());
    private static final ZoneId ZONE = ZoneId.of("GMT+8");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int HOURS_BACK = 5; // 回溯5小时
    private static final int INTERVAL_HOURS = 1; // 每小时一个数据点
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserInfoViewRepository userInfoViewRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${dataLink}")
    private String url;

    public SessionInfo saveSession(SessionInfo sessionInfo) {
        String userId = sessionInfo.getUserId();
        LocalDateTime startTime = sessionInfo.getStartTime();
        LocalDateTime endTime = sessionInfo.getEndTime();
        UserInfo userInfo = userInfoViewRepository.findById(userId);
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setName("unknown");
            userInfo.setDepartment("unknown");
        }
        sessionInfo.setUserName(userInfo.getName());
        sessionInfo.setDepartment(userInfo.getDepartment());
        sessionInfo.setDuration(Duration.between(startTime, endTime).toMillis());
        logger.info("保存会话信息: {}", sessionInfo);
        return sessionRepository.save(sessionInfo);
    }

    public List<SessionInfo> getAllSessions() {
        return sessionRepository.findAll();
    }

    /**
     * 活跃数计算：一段时间内至少启动一次应用/访问一次网站的独立用户数
     */
    public int getActiveUser(String startDateStr, String endDateStr) {
        // Step 1: 解析起止时间
        Instant startUtc = changeDateToUTC(startDateStr);
        Instant endUtc = changeDateToUTC(endDateStr);
        logger.info("日活跃数计算查询时间范围: {} ~ {}", startUtc, endUtc);
        // Step 2: 构建聚合查询
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("startTime").gte(startUtc).lt(endUtc)), // 过滤时间范围
                Aggregation.group("userId"), // 按 userId 去重
                Aggregation.count().as("total") // 统计总数
        );
        // Step 3: 执行查询
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "Session", // MongoDB 集合名
                Document.class
        );
        return results.getMappedResults().isEmpty() ? 0 : results.getMappedResults().get(0).getInteger("total");
    }

    public DataRatio getActiveUserRatio(String startDateStr, String endDateStr) throws Exception {
        String[] prevTimes = getPreviousPeriod(startDateStr, endDateStr);
        int prevActiveUser = getActiveUser(prevTimes[0], prevTimes[1]);
        int activeUser = getActiveUser(startDateStr, endDateStr);
        double ratio = (activeUser == 0) ?
                (prevActiveUser == 0 ? 0.0 : 1.0) :   // 处理除数为0的情况
                (prevActiveUser - activeUser) * 1.0 / activeUser;
        return new DataRatio((double) activeUser, (double) prevActiveUser, ratio);
    }

    /**
     * 平均会话时长：24小时内平均会话时长
     */
    public Double getAverageSessionTime(String dateStr) {
        // Step 1: 解析 GMT+8 日期的起止时间
        DateRange dateRange = Utils.getDateRange(dateStr);
        logger.info("平均会话时长查询日期: {}", dateRange);
        // Step 2: 转换为 UTC 时间用于查询
        Instant startUtc = dateRange.getStart();
        Instant endUtc = dateRange.getEnd();
        logger.info("平均会话时长查询时间范围: {} ~ {}", startUtc, endUtc);
        // Step 3: 构建聚合查询
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("startTime").gte(startUtc).lt(endUtc)), // 过滤时间范围
                Aggregation.group().avg("duration").as("avgDuration")
        );
        // Step 4: 执行查询
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "Session", // MongoDB 集合名
                Document.class
        );
        // 从结果中提取平均值
        double averageMs = results.getMappedResults().isEmpty() ? 0 : results.getMappedResults().get(0).getDouble("avgDuration");
        // 转换为分钟，保留两位小数
        return Math.round((averageMs / 60_000) * 100.0) / 100.0;
    }

    /**
     * todo 通过接口获得数据
     */
    public List<OperationMetrics> getOperationMetrics(String startTime, String endTime) {
        // 时区转换
        String startTimeUtc = Utils.localToUTC(startTime);
        String endTimeUtc = Utils.localToUTC(endTime);
        logger.info("查询操作指标: {} ~ {}", startTimeUtc, endTimeUtc);
        // 创建请求体对象
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("start_time", startTimeUtc);
        requestBody.put("end_time", endTimeUtc);
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建请求实体
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        // 发送POST请求
        ResponseEntity<List<OperationMetrics>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<OperationMetrics>>() {
                }
        );
        // 解析并返回数据
        return response.getBody();
    }

    /**
     * todo 总查询次数
     */
    public DataRatio getTotalQueryCount(String startTime, String endTime) throws Exception {
        String[] prevTimes = getPreviousPeriod(startTime, endTime);
        List<OperationMetrics> operationMetrics = getOperationMetrics(startTime, endTime);
        List<OperationMetrics> prevMetrics = getOperationMetrics(prevTimes[0], prevTimes[1]);
        int first_num = (operationMetrics != null) ? operationMetrics.size() : 0;
        int second_num = (prevMetrics != null) ? prevMetrics.size() : 0;
        double ratio = (second_num == 0) ?
                (first_num == 0 ? 0.0 : 1.0) :   // 处理除数为0的情况
                (first_num - second_num) * 1.0 / second_num;
        return new DataRatio((double) first_num, (double) second_num, ratio);
    }

    /**
     * todo 平均响应时间计算
     */
    public double getAverageResponseTime(String startTime, String endTime) {
        List<OperationMetrics> operationMetrics = getOperationMetrics(startTime, endTime);
        if (operationMetrics.isEmpty()) {
            return 0;
        }
        long totalTime = 0;
        int validCount = 0;
        for (OperationMetrics metric : operationMetrics) {
            LocalDateTime start = metric.getStartTime();
            LocalDateTime end = metric.getEndTime();
            // 跳过无效时间记录
            if (start == null || end == null) {
                continue;
            }
            // 计算持续时间（纳秒精度）
            Duration duration = Duration.between(start, end);
            totalTime += duration.toMillis();
            validCount++;
        }
        // 处理全无效数据的情况
        if (validCount == 0) {
            return 0;
        }
        // 转换为毫秒并保留3位小数
        double averageMillis = (double) totalTime / validCount;
        return Math.round(averageMillis * 1000.0) / 1000.0;
    }

    public DataRatio getAverageResponseTimeRatio(String startTime, String endTime) throws Exception {
        String[] prevTimes = getPreviousPeriod(startTime, endTime);
        double first_num = getAverageResponseTime(startTime, endTime);
        double second_num = getAverageResponseTime(prevTimes[0], prevTimes[1]);
        double ratio = (second_num == 0) ?
                (first_num == 0 ? 0.0 : 1.0) :   // 处理除数为0的情况
                (first_num - second_num) / second_num;
        return new DataRatio(first_num, second_num, ratio);
    }

    /**
     * todo 分类占比
     */
    public Map<String, Integer> getCategoryRatio(String startTime, String endTime) {
        List<OperationMetrics> operationMetrics = getOperationMetrics(startTime, endTime);
        if (operationMetrics.isEmpty()) {
            return null;
        }
        Map<String, Integer> categoryRatio = new HashMap<>();
        for (OperationMetrics metric : operationMetrics) {
            if ("0".equals(metric.getQuestionCategory())) {
                categoryRatio.put("通用模式", categoryRatio.getOrDefault("通用模式", 0) + 1);
            } else {
                String domain = metric.getDomain();
                categoryRatio.put(domain, categoryRatio.getOrDefault(domain, 0) + 1);
            }
        }
        return categoryRatio;
    }

    /**
     * todo 用户留存情况
     */
    public double getUserRetention(String dateStrToday, String dateStrYesterday) {
        //获取当天的数据
        Set<UserInfo> userIdToday = getUserIdByDate(dateStrToday);
        //获取前一天的数据
        Set<UserInfo> userIdYesterday = getUserIdByDate(dateStrYesterday);
        if (userIdToday == null || userIdYesterday == null || userIdYesterday.isEmpty() || userIdToday.isEmpty()) {
            return 0.0;
        }
        //获取重复数据
        Set<UserInfo> intersection = userIdToday.stream()
                .filter(userIdYesterday::contains)
                .collect(Collectors.toSet());
        // 计算留存率并保留两位小数
        double ratio = (double) intersection.size() / userIdYesterday.size();
        double retentionRate = ratio * 100; // 转换为百分比
        retentionRate = Math.round(retentionRate * 100) / 100.0; // 四舍五入保留两位小数

        return retentionRate;
    }

    /**
     * 通过日期获取用户信息
     *
     * @param dateStr 日期字符串
     * @return 日期对应的用户信息集合
     */
    public Set<UserInfo> getUserIdByDate(String dateStr) {
        // Step 1: 解析 GMT+8 日期的起止时间
        DateRange dateRange = Utils.getDateRange(dateStr);
        logger.info("用户留存查询日期: {}", dateRange);
        // Step 2: 转换为 UTC 时间用于查询
        Instant startUtc = dateRange.getStart();
        Instant endUtc = dateRange.getEnd();
        logger.info("用户留存查询时间范围: {} ~ {}", startUtc, endUtc);
        // Step 3: 构建聚合查询
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("startTime").gte(startUtc).lt(endUtc)), // 过滤时间范围
                Aggregation.group("userId") // 按 userId 去重
        );
        // Step 4: 执行查询
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "Session", // MongoDB 集合名
                Document.class
        );
        // Step 5: 提取用户ID到Set
        Set<String> Ids = results.getMappedResults()
                .stream()
                .map(document -> document.getString("_id"))
                .collect(Collectors.toSet());
        return Ids.stream()
                .map(id -> userInfoViewRepository.findById(id))
                .collect(Collectors.toSet());
    }

    //todo 计算折线图的平均响应时间
    public Map<String, Double> getHourlyResponseTimes() {
        // 获取当前时间并向下取整到整点
        LocalDateTime currentTime = LocalDateTime.now(ZONE);
        LocalDateTime endHour = currentTime.truncatedTo(ChronoUnit.HOURS);
        // 创建结果映射（LinkedHashMap保证顺序）
        Map<String, Double> results = new LinkedHashMap<>();
        // 生成5小时内的整点时间点（从最早到最近）
        for (int i = HOURS_BACK; i > 0; i--) {
            // 计算当前时间段的结束时间（较近的时间点）
            LocalDateTime segmentEnd = endHour.minusHours(i - 1);
            // 计算当前时间段的开始时间（较远的时间点）
            LocalDateTime segmentStart = segmentEnd.minusHours(INTERVAL_HOURS);
            // 格式化时间范围
            String startStr = segmentStart.format(FORMATTER);
            String endStr = segmentEnd.format(FORMATTER);
            // 获取平均响应时间
            double avg = getAverageResponseTime(startStr, endStr);
            // 保存结果（以结束时间点作为键）
            results.put(segmentEnd.format(FORMATTER), avg);
        }

        // 添加当前时间段（特殊处理）
        String currentStartStr = endHour.format(FORMATTER);
        String currentEndStr = currentTime.format(FORMATTER);
        double currentAvg = getAverageResponseTime(currentStartStr, currentEndStr);
        results.put(endHour.format(FORMATTER), currentAvg);

        return results;
    }

    public void Kmeans(String startTime, String endTime) {
        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        List<String> questions = getOperationMetrics(startTime, endTime)
                .stream()
                .map(metric -> metric.getRequest().getQuestion())
                .collect(Collectors.toList());
        requestBody.put("questions", questions);
        requestBody.put("algorithm", "kmeans");
        requestBody.put("k_min", 2);
        requestBody.put("k_max", 5);
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构造HttpEntity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        // 发送POST请求
        String url = "http://10.140.248.145:5000/cluster";
        ResponseEntity<List<Kmeans>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<Kmeans>>() {
                }
        );
        logger.info(Objects.requireNonNull(response.getBody()).toString());
    }

    private Instant changeDateToUTC(String dateStr) {
        // 1. 创建 GMT+8 时区对象
        ZoneId gmt8Zone = ZoneId.of("GMT+8");
        // 2. 创建日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 3. 解析字符串并直接创建带时区的日期时间对象
        ZonedDateTime gmt8ZonedDateTime = LocalDateTime
                .parse(dateStr, formatter)
                .atZone(gmt8Zone);  // 使用 atZone() 附加时区信息
        // 4. 转换为 Instant（UTC 时间）
        return gmt8ZonedDateTime.toInstant();
    }

    private String[] getPreviousPeriod(String startTime, String endTime) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date endDate = sdf.parse(endTime);
        Date startDate = sdf.parse(startTime);

        // 计算时间段长度（毫秒）
        long duration = endDate.getTime() - startDate.getTime();

        // 计算上一个时间段的开始和结束时间
        Date prevStartDate = new Date(startDate.getTime() - duration);

        return new String[]{
                sdf.format(prevStartDate),
                sdf.format(startDate)
        };
    }
}
