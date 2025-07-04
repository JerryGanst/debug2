package org.example.ai_api.Controller;

import cn.hutool.core.date.DateTime;
import org.example.ai_api.Bean.Entity.SessionInfo;
import org.example.ai_api.Bean.Entity.UserInfo;
import org.example.ai_api.Bean.Model.DataRatio;
import org.example.ai_api.Bean.Model.OperationMetrics;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 运维数据相关接口.
 *
 * @author 10353965
 */
@RestController
@RequestMapping("/Data")
public class DataController {
    private static final Logger logger = LoggerFactory.getLogger(DataController.class.getName());
    @Autowired
    private DataService dataService;

    /**
     * 保存会话信息.
     *
     * @param sessionInfo 保存的会话信息
     * @return 带有被保存结果id的数据
     */
    @PostMapping("/saveSession")
    @ResponseBody
    public ResultData<String> saveSession(@RequestBody SessionInfo sessionInfo) {
        logger.info("save session: {} {} {}", sessionInfo.getUserId(), sessionInfo.getStartTime(), sessionInfo.getEndTime());
        return ResultData.success("保存成功", dataService.saveSession(sessionInfo).getId());
    }

    /**
     * 获得所有会话信息.
     *
     * @return 所有会话信息
     */
    @PostMapping("/getAllSession")
    @ResponseBody
    public ResultData<List<SessionInfo>> getSessionInfoList() {
        logger.info("get session info list");
        return ResultData.success(dataService.getAllSessions());
    }

    /**
     * 获得一段时间的活跃用户数.
     *
     * @param startTime 查询的开始时间
     * @param endTime   查询的结束时间
     * @return 一段时间活跃用户数
     */
    @PostMapping("/getDailyActiveUser")
    @ResponseBody
    public ResultData<DataRatio> getActiveUser(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) throws Exception {
        logger.info("get active user: {} {}", startTime, endTime);
        return ResultData.success("获取成功", dataService.getActiveUserRatio(startTime, endTime));
    }

    /**
     * 获得某段时间的平均会话时长.
     *
     * @param dateStr 查询的目标日期
     * @return 某段时间的平均会话时长
     */
    @PostMapping("/getAverageSessionTime")
    @ResponseBody
    public ResultData<Double> getAverageSessionTime(@RequestParam("date") String dateStr) {
        logger.info("get average session time: {}", dateStr);
        return ResultData.success(dataService.getAverageSessionTime(dateStr));
    }

    /**
     * 获得某段时间的查询列表.
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 操作指标
     */
    @PostMapping("/getTotalQuery")
    @ResponseBody
    public ResultData<List<OperationMetrics>> getTotalQueryList(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        logger.info("getTotalQuery");
        List<OperationMetrics> data = dataService.getOperationMetrics(startTime, endTime);
        return ResultData.success("获取成功", data);
    }

    /**
     * 获得某段时间的查询总数.
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 查询总数
     */
    @PostMapping("/getTotalQueryCount")
    @ResponseBody
    public ResultData<DataRatio> getTotalQueryCount(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) throws Exception {
        logger.info("getTotalQueryCount");
        return ResultData.success("获取成功", dataService.getTotalQueryCount(startTime, endTime));
    }

    /**
     * 获得某段时间的使用功能分类
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分类信息
     */
    @PostMapping("/getCategoryRatio")
    @ResponseBody
    public ResultData<Map<String, Integer>> getCategoryRatio(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        logger.info("getCategoryRatio");
        Map<String, Integer> data = dataService.getCategoryRatio(startTime, endTime);
        logger.info("getCategoryRatio: {}", data);
        return ResultData.success("获取成功", data);
    }

    /**
     * 获得某段时间的用户留存率
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 用户留存率
     */
    @PostMapping("/getUserRetention")
    @ResponseBody
    public ResultData<Map<String, Double>> getUserRetention(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        logger.info("getUserRetention: {} {}", startTime, endTime);
        Map<String, Double> data = new LinkedHashMap<>();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 解析为 LocalDateTime，再转为 LocalDate
        LocalDate start = LocalDateTime.parse(startTime, formatter).toLocalDate();
        LocalDate end = LocalDateTime.parse(endTime, formatter).toLocalDate();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String currentDay = date.toString();
            String nextDay = date.plusDays(1).toString();
            data.put(currentDay, dataService.getUserRetention(currentDay, nextDay));
        }
        return ResultData.success("获取成功", data);
    }

    /**
     * 获得某段时间的平均响应时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 平均响应时间
     */
    @PostMapping("/getAverageResponseTime")
    @ResponseBody
    public ResultData<DataRatio> getAverageResponseTime(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) throws Exception {
        logger.info("getAverageResponseTime");
        return ResultData.success("获取成功", dataService.getAverageResponseTimeRatio(startTime, endTime));
    }

    /**
     * 以当前时间为节点，获得前五个小时每个小时的平均响应时间
     *
     * @return 五个小时的每个小时的平均响应时间
     */
    @PostMapping("/getHourlyResponseTime")
    @ResponseBody
    public ResultData<Map<String, Double>> getResponseTime() {
        logger.info("getHourlyResponseTime:{}", DateTime.now());
        return ResultData.success("获取成功", dataService.getHourlyResponseTimes());
    }

    /**
     * 获得某天的用户信息
     *
     * @param dateStr 查询的目标日期
     * @return 用户信息
     */
    @PostMapping("/getDailyActiveUserByDate")
    @ResponseBody
    public ResultData<Set<UserInfo>> getDailyActiveUserByDate(@RequestParam("date") String dateStr) {
        logger.info("getDailyActiveUserByDate");
        return ResultData.success("获取成功", dataService.getUserIdByDate(dateStr));
    }

    @PostMapping("/test")
    @ResponseBody
    public ResultData<String> test() {
        logger.info("test");
        dataService.Kmeans("2020-01-01 00:00:00", "2020-01-02 00:00:00");
        return ResultData.success("test");
    }
}
